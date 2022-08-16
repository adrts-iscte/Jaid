package scenarios.methodTransformations.left;

//d0779f95-d537-4501-b708-fc50747e6616
class Class {

    //dd35e588-8a52-4b6e-a084-0f46049da9ad
    private int newMethodName(int param1) {
        int t = param1;
        return t;
    }

    //0f1bef85-dcce-4bcb-8699-0111031df9c5
    private int method(int param1, int param2) {
        return param1 + param2;
    }

    //de35e588-8a52-3b6e-a084-0f46469da9ad
    private int callingOneParamMethod() {
        return newMethodName(1);
    }

    //d125e588-8a52-3b6e-a5484-0f46759da9ad
    private int callingTwoParamMethod() {
        return method(2, 3) + callingOneParamMethod();
    }

    private void methodToBeAdded() {}

    //5f604c49-f4a5-47c3-986f-f36ae17b44f5
    private double methodToBeAllChanged(double integer) {
        return (double) integer;
    }
}
