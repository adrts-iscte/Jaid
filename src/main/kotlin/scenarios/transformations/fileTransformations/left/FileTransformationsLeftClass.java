//177edfde-0e77-4931-bcca-3c8de691c400
package scenarios.transformations.fileTransformations.left;

//e41312d4-edc9-423a-a4d0-3b8d904b9624
class ClassRenamed {

    //24070121-8f3c-4dfd-a150-ff950376b0dc
    public ClassRenamed() {

    }
}

//d0779f95-d537-4501-b708-fc50747e6616
class ClassNotModified {

    //1d2ac771-5d6a-4d2d-b9d7-401c221e914a
    private final ClassRenamed classToBeRenamed = new ClassRenamed();
}

/**
 * classToBeAdded
 */
class ClassToBeAdded extends ClassNotModified {

    private boolean bool;

    void printBool() {
        System.out.println(bool);
    }

    // LineComment

    /*
        Block Comment
    */
}

interface InterfaceToBeAdded {

}

//e1679f95-d537-4501-b708-fc50747e6616
class ClassModified extends ClassNotModified implements InterfaceToBeAdded {

    int attribute;

    public ClassModified() {

    }

    void doNothing() {

    }


}

//d2679f95-d537-4501-b708-fc50747e6616
abstract interface InterfaceToBeModified {

    int attribute = 0;
}

//d2679f95-d564-4501-b708-fc50747e6616
interface InterfaceRenamed {

}