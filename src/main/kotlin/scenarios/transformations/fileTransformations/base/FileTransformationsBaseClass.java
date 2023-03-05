package scenarios.transformations.fileTransformations.base;

//e41312d4-edc9-423a-a4d0-3b8d904b9624
class ClassToBeRenamed {

    //24070121-8f3c-4dfd-a150-ff950376b0dc
    public ClassToBeRenamed() {

    }
}

//d0779f95-d537-4501-b708-fc50747e6616
class ClassNotModified {


    //1d2ac771-5d6a-4d2d-b9d7-401c221e914a
    private final ClassToBeRenamed classToBeRenamed = new ClassToBeRenamed();
}

//e2789f95-d637-4501-b708-fc50747e6616
class ClassToBeRemoved {

}

//e1679f95-d537-4501-b708-fc50747e6616
final class ClassToBeModified {

}

//f2784f95-d637-4501-b708-fc50747e6616
interface InterfaceToBeRemoved {

}

//d2679f95-d537-4501-b708-fc50747e6616
interface InterfaceToBeModified {

}

//d2679f95-d564-4501-b708-fc50747e6616
interface InterfaceToBeRenamed {

}