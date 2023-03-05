package scenarios.transformations.constructorTransformations.left;

//d0779f95-d537-4501-b708-fc50747e6616
class Class {

    //dd45f598-8a52-4c2e-a085-0f46049da9ad
    int attribute;

    //Constructor to be Added
    public Class(float param1) {
        //Do something
    }

    //de45e588-8a52-4b6e-a085-0f46049da9ad
    private Class(int newInteger) {
        Class instance = Class.this;
        this.attribute = newInteger;
    }

    //de45e588-8a52-4b6e-a085-0f46049da9fe
    private Class(boolean bool) {
        this(9);
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
