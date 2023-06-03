//67ededd4-ccb5-44db-985d-3e5092da763d
package scenarios.transformations.nestedTypeTransformations.left;

//f45a475f-38d9-4828-bf4f-dd1a2b6c22e2
class nestedTypeTransformationsClass {

    class nestedClassToBeInserted {}

    //406ae4f7-8558-4f5e-918e-470849d81111
    class nestedClassTobeModified {

        //406ae4f7-8558-4f5e-918e-470849d82222
        class nestedNestedClassToBeInserted {}

        //c70723d9-c1cd-4ba8-a274-ac01b414aa3c
        class classRenamed {
            //dd35e599-8a52-4b6e-a084-0f46049da9ad
            class nestedClassRenamed {}
        }

        //4f604c49-f4a5-47c3-986f-f36ae17b44f3
        int newAttribute;

        //dd35e588-8a52-4b6e-a084-0f46049da9ad
        private int newMethodName(int param1) {
            int t = param1;
            return t;
        }

        //0f1bef85-dcce-4bcb-8699-0111031df9c5
        private int method(int param1, int param2) {
            return param1 + param2 + newAttribute;
        }

        //de35e588-8a52-3b6e-a084-0f46469da9ad
        private int callingOneParamMethod() {
            return newMethodName(1);
        }

        //d125e588-8a52-3b6e-a584-0f46759da9ad
        private int callingTwoParamMethod() {
            return method(2, 3) + callingOneParamMethod();
        }

        private void methodToBeAddedNoComment() {}

        // asd
        private void methodToBeAddedLineComment() {}

        /**/
        private void methodToBeAddedEmptyBlockComment() {}

        /* asd */
        private void methodToBeAddedBlockComment() {}

        /**
         * param
         */
        private void methodToBeAddedJavaDocComment() {}

        //5f604c49-f4a5-47c3-986f-f36ae17b44f5
        private final double methodToBeAllChanged(boolean bool, double integer) {
            return (double) integer;
        }

        //dd35e588-8a52-4b6e-b085-0f46049da9aa
        void a() {

        }

        //dd35e588-8a52-4b6e-b085-0f46049da9ab
        void b() {

        }

        //dd35e588-8a52-4b6e-b085-0f46049da9ad
        void d() {

        }

        //dd35e588-8a52-4b6e-b085-0f46049da9ae
        void e() {

        }
    }
}
