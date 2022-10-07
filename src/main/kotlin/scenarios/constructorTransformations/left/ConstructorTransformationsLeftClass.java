package scenarios.constructorTransformations.left;

//d0779f95-d537-4501-b708-fc50747e6616
class Class {

    //dd45f598-8a52-4c2e-a085-0f46049da9ad
    int attribute;

    public Class() {

    }

    //de45e588-8a52-4b6e-a085-0f46049da9ad
    private Class(int newInteger) {
        this.attribute = newInteger;
    }

    //c2be7ff1-d45d-4115-be4a-672c1738a532
    public Class(double dbl) {
        newMethodName(1);
    }

    //dd35e588-8a52-4b6e-a084-0f46049da9ad
    private int newMethodName(int param1) {
        int t = param1;
        return t;
    }

}
