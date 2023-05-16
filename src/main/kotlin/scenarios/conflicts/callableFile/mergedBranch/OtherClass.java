package scenarios.conflicts.callableFile.mergedBranch;

//0932cc71-d4a6-4b1e-8e8a-089224de8cda
class OtherClass {

    public OtherClass() {
        EnumToBeModified enumConstantToBeRemoved = EnumToBeModified.ENUM_CONSTANT_TO_BE_REMOVED;
    }

    //b2a16f06-ceaa-422c-98fd-9b67cf37c478
    void methodBodychanged() {
        EnumToBeModified enumConstantToBeRemoved = EnumToBeModified.ENUM_CONSTANT_TO_BE_REMOVED;
    }
}

//bacf47f8-af9e-46f9-af12-46a02996d192
enum EnumToBeModified{

    //c979c7b2-1770-463f-b071-d0f91214418b
    ENUM_CONSTANT_TO_BE_REMOVED
}
