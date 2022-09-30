package scenarios.constructorTransformations.left;

class Class {
    //d0779f95-d537-4501-b708-fc50747e6616

    //dd45f598-8a52-4c2e-a085-0f46049da9ad
    int attribute;

    public Class() {
        //dd35e588-8a52-4b6e-a085-0f46049da9ad
        ;
    }

    private Class(int newInteger) {
        //de45e588-8a52-4b6e-a085-0f46049da9ad
        ;
        this.attribute = newInteger;
    }

    public Class(double dbl) {
        //c2be7ff1-d45d-4115-be4a-672c1738a532
        ;
        newMethodName(1);
    }

    private int newMethodName(int param1) {
        //dd35e588-8a52-4b6e-a084-0f46049da9ad
        ;
        int t = param1;
        return t;
    }

}
