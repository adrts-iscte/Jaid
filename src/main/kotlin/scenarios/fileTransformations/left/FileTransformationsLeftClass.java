package scenarios.fileTransformations.left;

import java.util.*;

class ClassNotModified {
    //d0779f95-d537-4501-b708-fc50747e6616
}

class ClassToBeAdded {

}

interface InterfaceToBeAdded {

}

class ClassModified {
    //e1679f95-d537-4501-b708-fc50747e6616

    //4f604c49-f4a5-47c3-986f-f36ae17b44f5
    int attribute;

    public ClassModified() {
        //3a97115d-7307-4b31-aa79-10b07deb019d
        ;
    }

    void doNothing() {
        //e0c128d7-a8cc-4d80-a42e-ec897ed5b3d4
        ;
    }
}

interface InterfaceToBeModified {
    //d2679f95-d537-4501-b708-fc50747e6616

    //5f604c49-f4a5-47c3-986f-f36ae17b44f5
    int attribute = 0;
}