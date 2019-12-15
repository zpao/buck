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
package com.facebook.buck.features.python;

import static org.junit.Assert.assertThat;

import com.facebook.buck.core.sourcepath.PathSourcePath;
import com.facebook.buck.core.sourcepath.SourcePath;
import com.facebook.buck.io.filesystem.ProjectFilesystem;
import com.facebook.buck.io.filesystem.impl.FakeProjectFilesystem;
import java.util.ArrayList;
import java.util.List;
import org.hamcrest.Matchers;
import org.junit.Test;

public class PythonModuleDirComponentsTest {

  @Test
  public void forEachInput() {
    ProjectFilesystem filesystem = new FakeProjectFilesystem();
    SourcePath dir = PathSourcePath.of(filesystem, filesystem.getPath("dir"));
    PythonComponents components = PythonModuleDirComponents.of(dir);
    List<SourcePath> inputs = new ArrayList<>();
    components.forEachInput(inputs::add);
    assertThat(inputs, Matchers.contains(dir));
  }
}