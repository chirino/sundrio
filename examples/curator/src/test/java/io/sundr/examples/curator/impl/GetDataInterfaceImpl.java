/*
 * Copyright 2015 The original authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package io.sundr.examples.curator.impl;


import io.sundr.examples.curator.BackgroundableToPathableInterfaceOrPathableInterface;
import io.sundr.examples.curator.GetDataInterface;
import io.sundr.examples.curator.Pathable;

public class GetDataInterfaceImpl implements GetDataInterface, BackgroundableToPathableInterfaceOrPathableInterface<String> {
    
    @Override
    public Pathable<String> inBackground(boolean flag) {
        return this;
    }

    @Override
    public Pathable<String> inBackground(Object context) {
        return this;
    }

    @Override
    public Pathable<String> inBackground() {
        return this;
    }

    @Override
    public String forPath(String path) {
        return null;
    }

    @Override
    public BackgroundableToPathableInterfaceOrPathableInterface<String> watched() {
        return this;
    }


    static {
        new GetDataInterfaceImpl().watched().forPath("somePath");
        new GetDataInterfaceImpl().inBackground().forPath("somePath");
        new GetDataInterfaceImpl().forPath("somePath");
        new GetDataInterfaceImpl().inBackground().forPath("somePath");
        new GetDataInterfaceImpl().inBackground().forPath("somePath");
    }
}