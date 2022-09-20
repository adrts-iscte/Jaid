package scenarios.fieldTransformations.left;

//d0779f95-d537-4501-b708-fc50747e6616
class Class {

    //0f1bef85-dcce-4bcb-8699-0111031df9c5
    private double attribute;

    //4f604c49-f4a5-47c3-986f-f36ae17b44f5
    int newAttribute;

    //5e604c59-f4a5-47d3-986f-f36ae17b44f5
    boolean bool = true;

    private int method(int attribute) {
        //dd35e588-8a52-4b6e-a084-0f46049da9ad
        ;
        return (int) (attribute + 1);
    }

    private int moreMethod() {
        //dd353588-8ae2-4b6e-a084-0f46049da9ad
        ;
        int value = newAttribute;
        method(newAttribute);
//        double attribute = 0.0;
        return (int) (attribute + 1);
    }
}
