/* Copyright 2021 The TensorFlow Authors. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================*/

package org.tensorflow.lite;

import java.io.File;
import java.lang.reflect.Constructor;
import java.nio.ByteBuffer;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Factory for constructing InterpreterApi instances.
 *
 * <p>This one is a proxy for the actual TF Lite runtime's implementation factory.
 */
public class InterpreterFactory {
  public InterpreterFactory() {}

  /**
   * Constructs an {@link InterpreterApi} instance, using the specified model and options. The model
   * will be loaded from a file.
   *
   * @param modelFile A file containing a pre-trained TF Lite model.
   * @param options A set of options for customizing interpreter behavior.
   * @throws IllegalArgumentException if {@code modelFile} does not encode a valid TensorFlow Lite
   *     model.
   */
  public InterpreterApi create(@NonNull File modelFile, InterpreterApi.Options options) {
    InterpreterFactoryApi factory = getFactory();
    return factory.create(modelFile, options);
  }

  /**
   * Constructs an {@link InterpreterApi} instance, using the specified model and options. The model
   * will be read from a {@code ByteBuffer}.
   *
   * @param byteBuffer A pre-trained TF Lite model, in binary serialized form. The ByteBuffer should
   *     not be modified after the construction of an {@link InterpreterApi} instance. The {@code
   *     ByteBuffer} can be either a {@code MappedByteBuffer} that memory-maps a model file, or a
   *     direct {@code ByteBuffer} of nativeOrder() that contains the bytes content of a model.
   * @param options A set of options for customizing interpreter behavior.
   * @throws IllegalArgumentException if {@code byteBuffer} is not a {@code MappedByteBuffer} nor a
   *     direct {@code ByteBuffer} of nativeOrder.
   */
  public InterpreterApi create(@NonNull ByteBuffer byteBuffer, InterpreterApi.Options options) {
    InterpreterFactoryApi factory = getFactory();
    return factory.create(byteBuffer, options);
  }

  static InterpreterFactoryApi getFactory() {
    InterpreterFactoryApi factory;
    try {
      Class<?> clazz = Class.forName("org.tensorflow.lite.InterpreterFactoryImpl");
      factory = (InterpreterFactoryApi) clazz.getDeclaredConstructor().newInstance();
      // It would suffice to catch ReflectiveOperationException, but that requires API level 19.
    } catch (Exception e) {
      try {
        Class<?> clazz = Class.forName("com.google.android.gms.tflite.InterpreterFactoryImpl");
        Constructor<?> constructor = clazz.getDeclaredConstructor();
        constructor.setAccessible(true);
        factory = (InterpreterFactoryApi) constructor.newInstance();
      } catch (Exception e2) {
        e.addSuppressed(e2);
        throw new IllegalStateException(
            "Failed to create the InterpreterFactoryImpl dynamically -- "
                + "make sure your app links in the right TensorFlow Lite runtime. "
                + "You should declare a build dependency on either "
                + "org.tensorflow.lite:tensorflow-lite or "
                + "com.google.android.gms:play-services-tflite-java",
            e);
      }
    }
    return factory;
  }
}
