package scenarios.conflicts.fileFile.mergedBranch;

//2521977f-0c70-486f-98c1-39976894fc71
class OtherClass{}

//e3255b6a-7a08-45de-8abc-b72f69f3bd8a
interface InterfaceToBeAdded{}

//837f444a-471d-4aa9-b8e0-a3ba5ee5a05b
class classWithoutDifferentTwoNames{}

//837f444a-471d-4aa9-b8e0-a3ba5ee5a05b
class ClassWithConflictingTwoNames{}

//d2d5fb52-26ba-4b2c-8a81-4cc5da127e34
class ClassToHaveConflictingTwoNames{}

//48260ea4-ed59-46eb-93de-2ac33541bb2e
enum EnumToBeRemoved {

    //ff1b7228-2d03-4075-b64a-3aed7750e235
    ENUM_CONSTANT_ARGUMENTS(10);

    //5bc14d6f-62a3-44bb-aa23-c322fa20b3df
    EnumToBeRemoved(int integer) {}

}

//11789f06-0d3f-4f2c-b537-1a89fbb8bac6
enum EnumToHaveEnumConstantToBeRemoved {

    //8b12532d-00bc-403f-b3d0-6946ab308410
    ENUM(10),

    //960aa7ce-38b8-44fb-ac95-45b311946765
    ENUM_TO_BE_REMOVED(50);

    //5bc14d6f-62a3-44bb-aa23-c322fa20b3df
    EnumToHaveEnumConstantToBeRemoved(int integer) {}
}