package scenarios.sequenceTransformations.base;

//d0779f95-d537-4501-b708-fc50747e6616
class Class {

    //4f604c49-f4a5-47c3-986f-f36ae17b44f5
    int attribute;

    //dd35e588-8a52-4b6e-a084-0f46049da9ad
    private int newMethodName(int attribute) {
        return (int) (attribute + 1);
    }

    //dd353588-8ae2-4b6e-a084-0f46049da9ad
    private int moreMethod() {
        int value = attribute;
        newMethodName(attribute);
//        double attribute = 0.0;
        return (int) (attribute + 1);
    }
}
