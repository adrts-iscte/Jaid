package scenarios.methodTransformations.base;

//d0779f95-d537-4501-b708-fc50747e6616
class Class {

    private int method(int param1) {
        //dd35e588-8a52-4b6e-a084-0f46049da9ad
        ;
        int t = param1;
        return t;
    }

    private int method(int param1, int param2) {
        //0f1bef85-dcce-4bcb-8699-0111031df9c5
        ;
        return param1 + param2;
    }

    private int callingOneParamMethod() {
        //de35e588-8a52-3b6e-a084-0f46469da9ad
        ;
        return method(1);
    }

    private int callingTwoParamMethod() {
        //d125e588-8a52-3b6e-a584-0f46759da9ad
        ;
        return method(2, 3) + callingOneParamMethod();
    }

    private int methodToBeRemoved() {
        //4f604c49-f4a5-47c3-986f-f36ae17b44f5
        ;
        return callingOneParamMethod();
    }

    public int methodToBeAllChanged(int integer, boolean bool) {
        //5f604c49-f4a5-47c3-986f-f36ae17b44f5
        ;
        return integer;
    }
}
