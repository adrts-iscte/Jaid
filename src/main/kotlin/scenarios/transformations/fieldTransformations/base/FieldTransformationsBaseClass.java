//ab443b72-fe65-46a0-883c-48a62caf1787
package scenarios.transformations.fieldTransformations.base;

//d0779f95-d537-4501-b708-fc50747e6616
class Class {

    //0f1bef85-dcce-4bcb-8699-0111031df9c5
    int attribute;

    //4f604c40-f4a5-46c3-986f-f36ad17b44f5
    int attributeToBeRemoved;

    //5e604c59-f4a5-47d3-986f-f36ae17b44f5
    boolean bool;

    //3bf9f206-6045-4bef-8f7a-8e47b08f0a67
    double doub = 1.0;

    //dd35e588-8a52-4b6e-a084-0f46049da9ad
    private int newMethodName(int attribute) {
        return (int) (attribute + 1);
    }

    //dd353588-8ae2-4b6e-a084-0f46049da9ad
    private int moreMethod() {
        int value = this.attribute;
        newMethodName(attribute);
//        double attribute = 0.0;
        return (int) (attribute + 1);
    }
}
