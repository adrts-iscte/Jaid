package scenarios.conflicts.fileFile.branchToBeMerged;

//2521977f-0c70-486f-98c1-39976894fc71
class OtherClass {
}

//d9223348-adca-4651-bf44-cde8e5cb2cc6
class TypeToBeMoved{}

//5abacab9-a717-4cf6-91b4-0cc96e10894a
class outerClass {
    //8c1ae437-0334-4ac1-a05f-2bb4c8d068a7
    class TypeToBeAdded {
    }
}

//e5f8801c-a50a-48e9-89cd-142f29dc21a9
final class RemovedOnly extends TypeToBeMoved implements InterfaceToBeAdded {}

//29027bcd-f473-4daf-a9de-bde7a3a9551c
final class RemovedAndMoved extends TypeToBeMoved implements InterfaceToBeAdded {}

//114f11ee-8e90-40e3-88de-ed7a1a73bf70
interface InterfaceToBeAdded{}

//837f444a-471d-4aa9-b8e0-a3ba5ee5a05b
class classWithDifferentTwoNames{}

//837f444a-471d-4aa9-b8e0-a3ba5ee5a05b
class theAnotherClassToHaveConflictingTwoNames{}

//d2d5fb52-26ba-4b2c-8a81-4cc5da127e34
class ClassToHaveConflictingTwoNames{}

//11789f06-0d3f-4f2c-b537-1a89fbb8bac6
enum EnumToHaveEnumConstantToBeRemoved {

    //8b12532d-00bc-403f-b3d0-6946ab308410
    ENUM(10);

    //5bc14d6f-62a3-44bb-aa23-c322fa20b3df
    EnumToHaveEnumConstantToBeRemoved(int integer) {}
}