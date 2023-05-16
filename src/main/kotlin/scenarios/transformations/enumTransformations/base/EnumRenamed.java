package scenarios.transformations.enumTransformations.base;

//8bd78697-56ff-48df-99cd-65c86a67c050
enum EnumToBeRenamed {

    //2ed035c0-0ac4-4825-8841-3ea48fbbcf88
    ENUM_CONSTANT_TO_BE_REMOVED,

    //cd51a8ff-01dd-4f9b-9319-191533dbe7ed
    ENUM_CONSTANT_TO_BE_RENAMED;

    //4f604c49-f4a5-47c3-986f-f36ae17b44f3
    int attribute;

    //dd35e588-8a52-4b6e-a084-0f46049da9ad
    private int method(int param1) {
        int t = param1;
        return t;
    }

    //0f1bef85-dcce-4bcb-8699-0111031df9c5
    private int method(int param1, int param2) {
        return param1 + param2 + attribute;
    }

    //de35e588-8a52-3b6e-a084-0f46469da9ad
    private int callingOneParamMethod() {
        return method(1);
    }

    //d125e588-8a52-3b6e-a584-0f46759da9ad
    private int callingTwoParamMethod() {
        return method(2, 3) + callingOneParamMethod();
    }

    //4f604c49-f4a5-47c3-986f-f36ae17b44f5
    private int methodToBeRemovedLineComment() {
        return callingOneParamMethod();
    }

    /*f24c07e9-ee63-4745-98b7-454bd529e360*/
    private void methodToBeRemovedEmptyBlockComment() {}

    /*
     * test
     * 26a64227-9d49-4815-9bb0-fab66aeb16c7*/
    private void methodToBeRemovedBlockComment() {}

    /**
     * 1c4643a0-e0ca-4385-b3d7-8f35525aa31e*/
    private void methodToBeRemovedEmptyJavaDocComment() {}

    /**
     * param
     * 247163ba-903a-4eaa-8714-9aca0e6da060*/
    private void methodToBeRemovedJavaDocComment() {}

    //5f604c49-f4a5-47c3-986f-f36ae17b44f5
    public synchronized int methodToBeAllChanged(int integer, boolean bool) {
        return integer;
    }

    //dd35e588-8a52-4b6e-b085-0f46049da9ad
    void d() {

    }

    //dd35e588-8a52-4b6e-b085-0f46049da9ab
    void b() {

    }

    //dd35e588-8a52-4b6e-b085-0f46049da9aa
    void a() {

    }

    //dd35e588-8a52-4b6e-b085-0f46049da9ae
    void e() {

    }
}

//03541bda-9a2c-433a-8934-98025f481145
class asd {
    //9ff7d185-233f-42f1-b84d-a8bb646da5e2
    enum EnumToHaveEntriesMoved {
        //a998a89d-594f-47e8-b52b-22ae2fde3a7d
        D,

        //a998a89d-594f-47e8-b52b-22ae2fde3a7b
        B,

        //a998a89d-594f-47e8-b52b-22ae2fde3a7a
        A,

        //a998a89d-594f-47e8-b52b-22ae2fde3a7e
        E;
    }
}

//9f2a3eeb-ce6c-4d86-8582-278b5309626c
enum EnumToBeRemoved {}

//bbbbbbbb-ce6c-4d86-8582-278b5309626c
interface interfaceToBeImplemented {}

//aaaaaaaa-ce6c-4d86-8582-278b5309626c
class classToUseEnum {

    //31448b2d-8c71-461e-a1c6-08f40a427f6a
    void useEnum() {
        EnumToBeRenamed enumConstantToBeAdded = EnumToBeRenamed.ENUM_CONSTANT_TO_BE_RENAMED;
    }
}