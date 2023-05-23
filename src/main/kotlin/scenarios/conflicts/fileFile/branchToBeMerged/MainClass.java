package scenarios.conflicts.fileFile.branchToBeMerged;

import java.util.*;

//88629cb9-cf93-48f0-a5b2-8f72a673d53b
class MainClass {
}

//13d2bf34-c0a2-48cd-9aaa-ec18dfae9223
class SameClassAfterBeingMovedAndRenamed{}

//08174100-c860-4c5e-9bf0-9e5255116e99
class ToBeAddedAndRenamed{}

//54bb4759-f4f3-47db-9d70-07fcafe76991
class AddedAndRenamed{}

//83333cdf-bdda-4639-8ac9-516f13fe7683
final class ClassToHaveModifiersChanged{}

//2a7ca006-9899-4e5c-a610-0445faae5ada
class ClassToHaveImplementsAndExtendsTypesChanged extends MainClass implements NewInterface{}

//aacf90de-9a63-414c-b213-cf2d59df5a68
interface NewInterface{}

//56a106db-1e7e-470c-b5f2-976f1d872072
enum enumToBeRemoved {

    //6103d60b-a352-44e8-843c-e041b37cf4fb
    ENUM_RENAMED,

    ENUM_TO_BE_ADDED

}

//a11af5f0-d2d2-4e74-bc33-9308cb15c049
enum enumToBeModified {

    //cba5590a-4fef-43dc-a570-7b54e54492e1
    ENUM_RENAMED,

    //f5db30f1-0ad2-4b69-87d7-c36464c6b753
    ENUM_RENAMED_AND_REMOVED,

    //87966b63-5b11-4bc2-a984-f3022041530e
    ENUM_TO_HAVE_RENAMED_CONFLICT,

    //28b5015c-d510-49ae-98cb-def68f3733ae
    ENUM_TO_HAVE_RENAMED_CONFLICT_2,

    //8db350f3-6904-4ab9-9232-1d672a32a79b
    ENUM_TO_HAVE_TWO_DIFFERENT_RENAMES_1,

    NEW_ADDED_ENUM_CONSTANT
}

//25e7cda0-7673-474e-b0f9-c5b1bef90e30
enum EnumConstantArguments {

    //607254e3-18ed-4856-85e3-989799494cb7
    ENUM_CONSTANT_ARGUMENTS(1);

    //9def877d-1a59-4b5a-af69-0f0853b0f66a
    EnumConstantArguments(int integer) {}
}