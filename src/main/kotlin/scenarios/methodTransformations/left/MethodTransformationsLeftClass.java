package scenarios.methodTransformations.left;

//d0779f95-d537-4501-b708-fc50747e6616
class Class {

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
    private double methodToBeAllChanged(boolean bool, double integer) {
        return (double) integer;
    }
}