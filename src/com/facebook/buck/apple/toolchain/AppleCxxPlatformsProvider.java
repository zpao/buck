/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.facebook.buck.apple.toolchain;

import com.facebook.buck.core.model.Flavor;
import com.facebook.buck.core.model.FlavorDomain;
import com.facebook.buck.core.util.immutables.BuckStyleValue;
import com.facebook.buck.cxx.toolchain.CxxPlatformsSupplier;
import com.facebook.buck.cxx.toolchain.UnresolvedCxxPlatform;
import com.facebook.buck.cxx.toolchain.impl.StaticUnresolvedCxxPlatform;
import com.google.common.collect.ImmutableMap;

@BuckStyleValue
public abstract class AppleCxxPlatformsProvider implements CxxPlatformsSupplier {

  public static final String DEFAULT_NAME = "apple-cxx-platforms";

  public abstract FlavorDomain<AppleCxxPlatform> getAppleCxxPlatforms();

  /** @return {@link CxxPlatform} of all {@link AppleCxxPlatform}s */
  @Override
  public ImmutableMap<Flavor, UnresolvedCxxPlatform> getCxxPlatforms() {
    ImmutableMap.Builder<Flavor, UnresolvedCxxPlatform> cxxSystemPlatformsBuilder =
        ImmutableMap.builder();

    for (AppleCxxPlatform appleCxxPlatform : getAppleCxxPlatforms().getValues()) {
      cxxSystemPlatformsBuilder.put(
          appleCxxPlatform.getCxxPlatform().getFlavor(),
          new StaticUnresolvedCxxPlatform(appleCxxPlatform.getCxxPlatform()));
    }
    return cxxSystemPlatformsBuilder.build();
  }

  @Override
  public String getName() {
    return DEFAULT_NAME;
  }

  public static AppleCxxPlatformsProvider of(FlavorDomain<AppleCxxPlatform> appleCxxPlatforms) {
    return ImmutableAppleCxxPlatformsProvider.of(appleCxxPlatforms);
  }
}
