package scenarios.conflicts.fileFile.mergedBranch;

import java.lang.*;

//88629cb9-cf93-48f0-a5b2-8f72a673d53b
class MainClass {
}

//08174100-c860-4c5e-9bf0-9e5255116e99
class AddedAndRenamed{}

//72e78c4f-3fc6-4e9c-91fb-aa4785c6c477
class AddedWithEnumConstantRemoved{

    //343ce610-9fee-4b8b-8a42-4d93a3f903ce
    void method() {
        enumToBeModified enumConstantToBeRemoved = enumToBeModified.ENUM_CONSTANT_TO_BE_REMOVED;
    }
}

//83333cdf-bdda-4639-8ac9-516f13fe7683
abstract class ClassToHaveModifiersChanged{}

//2a7ca006-9899-4e5c-a610-0445faae5ada
class ClassToHaveImplementsAndExtendsTypesChanged extends NewClassToBeExtended implements OtherNewInterface {}

//aacf90de-9a63-414c-b213-cf2d59df5a68
interface OtherNewInterface{}

//6c2a54a7-3f54-4637-b7c4-1342957db599
class NewClassToBeExtended{}

//a11af5f0-d2d2-4e74-bc33-9308cb15c049
enum enumToBeModified {

    //cba5590a-4fef-43dc-a570-7b54e54492e1
    ENUM_TO_BE_RENAMED,

    ENUM_RENAMED,

    //87966b63-5b11-4bc2-a984-f3022041530e
    ENUM_TO_HAVE_RENAMED_CONFLICT_1,

    //28b5015c-d510-49ae-98cb-def68f3733ae
    ENUM_TO_HAVE_RENAMED_CONFLICT,

    //8db350f3-6904-4ab9-9232-1d672a32a79b
    ENUM_TO_HAVE_TWO_DIFFERENT_RENAMES_2,

    NEW_ADDED_ENUM_CONSTANT,

    //00179bd4-86fc-4b5e-8adc-7062377b77a0
    ENUM_CONSTANT_TO_BE_REMOVED
}

//25e7cda0-7673-474e-b0f9-c5b1bef90e30
enum EnumConstantArguments {

    //607254e3-18ed-4856-85e3-989799494cb7
    ENUM_CONSTANT_ARGUMENTS(20);

    //9def877d-1a59-4b5a-af69-0f0853b0f66a
    EnumConstantArguments(int integer) {}
}