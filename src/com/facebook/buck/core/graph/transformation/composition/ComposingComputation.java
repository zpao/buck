/*
 * Copyright 2019-present Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.facebook.buck.core.graph.transformation.composition;

import com.facebook.buck.core.graph.transformation.ComputationEnvironment;
import com.facebook.buck.core.graph.transformation.model.ComposedComputationIdentifier;
import com.facebook.buck.core.graph.transformation.model.ComposedKey;
import com.facebook.buck.core.graph.transformation.model.ComposedResult;
import com.facebook.buck.core.graph.transformation.model.ComputeKey;
import com.facebook.buck.core.graph.transformation.model.ComputeResult;
import com.facebook.buck.core.graph.transformation.model.ImmutableComposedKey;
import com.facebook.buck.core.graph.transformation.model.ImmutableComposedResult;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

/**
 * An implementation of {@link ComposedComputation} that takes a base {@link ComposedComputation}, a
 * {@link Composer}, and {@link Transformer} and returns a new {@link ComposedResult} of {@link
 * Result2} type.
 *
 * @param <Key1> the key of the base computation
 * @param <Result1> the result type of the base computation
 * @param <Result2> the result type of this computation
 */
public class ComposingComputation<
        Key1 extends ComputeKey<Result1>,
        Result1 extends ComputeResult,
        Result2 extends ComputeResult>
    implements ComposedComputation<Key1, Result1, Result2> {

  private final Composer<Key1, Result1> composer;
  private final Transformer<Result2> transformer;

  private final ComposedComputationIdentifier<Result1> baseIdentifier;
  private final ComposedComputationIdentifier<Result2> identifier;

  /**
   * Creates a composition that runs the base computation, uses the results to derive further
   * dependencies and computes a new result.
   *
   * @param baseComputationIdentifer the {@link
   *     com.facebook.buck.core.graph.transformation.model.ComputationIdentifier} of the base
   *     computation.
   * @param resultClass the result type class of the {@link ComposedResult} that this computation
   *     returns
   * @param composer the {@link Composer} that computes the dependencies of this computation based
   *     on the result of the base computation.
   * @param transformer the {@link Transformer} to run on the dependencies to return results of
   *     {@link Result2}
   */
  ComposingComputation(
      ComposedComputationIdentifier<Result1> baseComputationIdentifer,
      Class<Result2> resultClass,
      Composer<Key1, Result1> composer,
      Transformer<Result2> transformer) {
    this.composer = composer;
    this.transformer = transformer;
    this.baseIdentifier = baseComputationIdentifer;
    this.identifier = ComposedComputationIdentifier.of(baseComputationIdentifer, resultClass);
  }

  @Override
  public ComposedComputationIdentifier<Result2> getIdentifier() {
    return identifier;
  }

  @Override
  public ComposedResult<Result2> transform(
      ComposedKey<Key1, Result2> key, ComputationEnvironment env) throws Exception {
    // TODO(bobyf): figure out how to not do this computation twice
    ComposedResult<Result1> results =
        env.getDep(
            ImmutableComposedKey.of(key.getOriginKey(), baseIdentifier.getTargetResultClass()));
    ImmutableList.Builder<Result2> resultBuilder =
        ImmutableList.builderWithExpectedSize(results.asList().size());

    for (Result1 result : results) {
      resultBuilder.add(
          transformer.transform(
              Maps.filterKeys(
                  env.getDeps(), composer.transitionWith(key.getOriginKey(), result)::contains)));
    }
    return ImmutableComposedResult.of(resultBuilder.build());
  }

  @Override
  public ImmutableSet<? extends ComputeKey<? extends ComputeResult>> discoverDeps(
      ComposedKey<Key1, Result2> key, ComputationEnvironment env) throws Exception {
    ComposedResult<Result1> results =
        env.getDep(
            ImmutableComposedKey.of(key.getOriginKey(), baseIdentifier.getTargetResultClass()));
    ImmutableSet.Builder<ComputeKey<?>> depBuilder =
        ImmutableSet.builderWithExpectedSize(results.asList().size());
    for (Result1 result : results) {
      depBuilder.addAll(composer.transitionWith(key.getOriginKey(), result));
    }
    return depBuilder.build();
  }

  @Override
  public ImmutableSet<? extends ComputeKey<? extends ComputeResult>> discoverPreliminaryDeps(
      ComposedKey<Key1, Result2> key) {
    return ImmutableSet.of(
        ImmutableComposedKey.of(key.getOriginKey(), baseIdentifier.getTargetResultClass()));
  }
}