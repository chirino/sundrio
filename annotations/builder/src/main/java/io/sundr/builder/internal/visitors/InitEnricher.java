/*
 *      Copyright 2017 The original authors.
 *
 *      Licensed under the Apache License, Version 2.0 (the "License");
 *      you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 */

package io.sundr.builder.internal.visitors;

import io.sundr.builder.TypedVisitor;
import io.sundr.builder.internal.functions.Construct;
import io.sundr.builder.internal.functions.TypeAs;
import io.sundr.codegen.functions.Collections;
import io.sundr.codegen.functions.Optionals;
import io.sundr.codegen.model.ClassRef;
import io.sundr.codegen.model.ClassRefBuilder;
import io.sundr.codegen.model.Kind;
import io.sundr.codegen.model.Property;
import io.sundr.codegen.model.PropertyBuilder;
import io.sundr.codegen.model.TypeRef;
import io.sundr.codegen.utils.TypeUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static io.sundr.builder.Constants.DESCENDANTS;
import static io.sundr.builder.Constants.DESCENDANT_OF;
import static io.sundr.builder.internal.utils.BuilderUtils.isBuildable;
import static io.sundr.codegen.model.Attributeable.ALSO_IMPORT;
import static io.sundr.codegen.model.Attributeable.INIT;
import static io.sundr.codegen.model.Attributeable.INIT_FUNCTION;
import static io.sundr.codegen.model.Attributeable.LAZY_INIT;
import static io.sundr.codegen.utils.TypeUtils.isAbstract;


public class InitEnricher extends TypedVisitor<PropertyBuilder> {

    @Override
    public void visit(PropertyBuilder builder) {
        TypeRef typeRef = builder.buildTypeRef();
        TypeRef unwrapped = TypeAs.combine(TypeAs.UNWRAP_ARRAY_OF, TypeAs.UNWRAP_COLLECTION_OF, TypeAs.UNWRAP_OPTIONAL_OF).apply(typeRef);
        boolean isBuildable = isBuildable(unwrapped);
        boolean hasDescendants = false;

        if (!(typeRef instanceof ClassRef)) {
            return;
        }

        Property parent = (Property) builder.getAttributes().get(DESCENDANT_OF);
        if (parent != null) {
            typeRef = parent.getTypeRef();
            unwrapped = TypeAs.combine(TypeAs.UNWRAP_ARRAY_OF, TypeAs.UNWRAP_COLLECTION_OF, TypeAs.UNWRAP_OPTIONAL_OF).apply(typeRef);
        } else if (builder.getAttributes().containsKey(DESCENDANTS) && !((Collection)builder.getAttributes().get(DESCENDANTS)).isEmpty()) {
            hasDescendants = true;
        }

        List<TypeRef> arguments = ((ClassRef) typeRef).getArguments();

        TypeRef targetType = unwrapped;
        if (isBuildable || hasDescendants) {
            ClassRef unwarppedClassRef = (unwrapped instanceof ClassRef) ? (ClassRef) unwrapped : null;
            targetType = isAbstract(unwarppedClassRef) || unwarppedClassRef.getDefinition().getKind() == Kind.INTERFACE ? TypeAs.VISITABLE_BUILDER.apply(unwarppedClassRef) : TypeAs.BUILDER.apply(unwarppedClassRef.getDefinition()).toInternalReference();
        }

        boolean isArray = TypeUtils.isArray(typeRef);
        boolean isSet = TypeUtils.isSet(typeRef);
        boolean isList = TypeUtils.isList(typeRef);
        boolean isMap = TypeUtils.isMap(typeRef);
        boolean isOptional = TypeUtils.isOptional(typeRef);
        boolean isOptionalInt = TypeUtils.isOptionalInt(typeRef);
        boolean isOptionalDouble = TypeUtils.isOptionalDouble(typeRef);
        boolean isOptionalLong = TypeUtils.isOptionalLong(typeRef);

        if (isArray || isList) {
            ClassRef listRef = Collections.ARRAY_LIST.toReference(targetType);
            builder.addToAttributes(LAZY_INIT, "new " + listRef + "()")
                    .addToAttributes(INIT_FUNCTION, new Construct(Collections.ARRAY_LIST, targetType))
                    .addToAttributes(ALSO_IMPORT, Arrays.asList(targetType, listRef));
        } else if (isSet) {
            ClassRef setRef = Collections.LINKED_HASH_SET.toReference(unwrapped);
            builder.addToAttributes(LAZY_INIT, "new " + setRef + "()")
                    .addToAttributes(INIT_FUNCTION, new Construct(Collections.LINKED_HASH_SET, unwrapped))
                    .addToAttributes(ALSO_IMPORT, Arrays.asList(targetType, setRef));
        } else if (isMap) {
            ClassRef mapRef = Collections.LINKED_HASH_MAP.toReference(arguments);
            builder.addToAttributes(LAZY_INIT, "new " + mapRef + "()")
                    .addToAttributes(INIT_FUNCTION, new Construct(Collections.LINKED_HASH_MAP, arguments))
                    .addToAttributes(ALSO_IMPORT, Arrays.asList(targetType, mapRef));
        } else if (isOptional) {
            final ClassRef ref = new ClassRefBuilder(Optionals.OPTIONAL.toReference()).withArguments(java.util.Collections.EMPTY_LIST).build();
            builder.addToAttributes(INIT,  "Optional.empty()")
                    .addToAttributes(INIT_FUNCTION, new Construct(Optionals.OPTIONAL, targetType, "of"))
                    .addToAttributes(ALSO_IMPORT, Arrays.asList(targetType, ref));
        } else if (isOptionalDouble) {
            final ClassRef ref = Optionals.OPTIONAL_DOUBLE.toReference();
            builder.addToAttributes(INIT, "OptionalDouble.empty()")
                    .addToAttributes(INIT_FUNCTION, new Construct(Optionals.OPTIONAL_DOUBLE, targetType, "of"))
                    .addToAttributes(ALSO_IMPORT, Arrays.asList(targetType, ref));
        } else if (isOptionalInt) {
            final ClassRef ref = Optionals.OPTIONAL_INT.toReference();
            builder.addToAttributes(INIT, "OptionalInt.empty()")
                    .addToAttributes(INIT_FUNCTION, new Construct(Optionals.OPTIONAL_INT, targetType, "of"))
                    .addToAttributes(ALSO_IMPORT, Arrays.asList(targetType, ref));
        } else if (isOptionalLong) {
            final ClassRef ref = Optionals.OPTIONAL_LONG.toReference();
            builder.addToAttributes(INIT, "OptionalLong.empty()")
                    .addToAttributes(INIT_FUNCTION, new Construct(Optionals.OPTIONAL_LONG, targetType, "of"))
                    .addToAttributes(ALSO_IMPORT, Arrays.asList(targetType, ref));
        }
    }
}
