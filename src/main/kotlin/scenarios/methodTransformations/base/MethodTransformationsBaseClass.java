package scenarios.methodTransformations.base;

//d0779f95-d537-4501-b708-fc50747e6616
class Class {

    //4f604c49-f4a5-47c3-986f-f36ae17b44f3
    int attribute;

    //dd35e588-8a52-4b6e-a084-0f46049da9ad
    private int method(int param1) {
        int t = param1;
        return t;
    }

    //0f1bef85-dcce-4bcb-8699-0111031df9c5
    private int method(int param1, int param2) {
        return param1 + param2 + attribute;
    }

    //de35e588-8a52-3b6e-a084-0f46469da9ad
    private int callingOneParamMethod() {
        return method(1);
    }

    //d125e588-8a52-3b6e-a584-0f46759da9ad
    private int callingTwoParamMethod() {
        return method(2, 3) + callingOneParamMethod();
    }

    //4f604c49-f4a5-47c3-986f-f36ae17b44f5
    private int methodToBeRemovedLineComment() {
        return callingOneParamMethod();
    }

    /*f24c07e9-ee63-4745-98b7-454bd529e360*/
    private void methodToBeRemovedEmptyBlockComment() {}

    /*
    * test
    * 26a64227-9d49-4815-9bb0-fab66aeb16c7*/
    private void methodToBeRemovedBlockComment() {}

    /**
     * 1c4643a0-e0ca-4385-b3d7-8f35525aa31e*/
    private void methodToBeRemovedEmptyJavaDocComment() {}

    /**
     * param
     * 247163ba-903a-4eaa-8714-9aca0e6da060*/
    private void methodToBeRemovedJavaDocComment() {}

    //5f604c49-f4a5-47c3-986f-f36ae17b44f5
    public int methodToBeAllChanged(int integer, boolean bool) {
        return integer;
    }
}
