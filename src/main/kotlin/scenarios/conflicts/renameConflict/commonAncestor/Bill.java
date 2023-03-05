package scenarios.conflicts.renameConflict.commonAncestor;

//e17675b0-a87e-44c0-8482-12caf6d55aea
public class Bill {

    //3c3103a6-ad3a-424c-8d9c-2e4de4d749a4
    void clcBl() {
        //code to calculate bills
    }

    //2f2aaf83-939d-4a93-8ce5-74850236c30b
    void sumOfBills() {
        clcBl();
    }

    //84ad2261-92da-476a-9470-182e7ac64b08
    void subOfBills() {
        clcBl();
    }

    //f8f5197a-cdd5-464e-82e8-f728d516d5b2
    void divisionOfBills() {}

    //00155fad-0118-4bb1-9695-47986ef27c97
    void methodWithSameNameAndDifferentParameters(boolean param1) {}

    //5c6f2c92-328f-44d6-881e-21e24a8b885a
    void methodWithSameNameAndDifferentParameters(int param1) {}

    //27d38d68-917c-44a9-8034-f6c3f5685df6
    void methodToBeRenamed(int param1) {}

    //0a9c0bbf-8047-4778-b6d3-9e7e00d0ef65
    void methodToBeCompared(double param1) {}

    //0c6abd2a-a1b7-4791-8e6f-f82858f6c623
    void methodReturnTypeChanged() {}

    //1c699b59-6cd1-44a2-98c6-086b2b948d28
    void methodToBeRenamed() { }

    //cc227ee7-3de3-464c-9979-c60e743abc26
    void methodToBeRenamedAndSamSignature(int param1) { }

    //12296537-d38d-46f6-98d4-36a41d0b5250
    void methodToBeRenamedAndDifferentSignature(int param2) { }

    //6ab885ef-9912-428f-9ac9-cc1b86dfa7f2
    protected void methodToModifiersChangedConflictious() { }

    //1e6bbf4d-a099-491c-8a5e-c805446cc486
    protected void methodToModifiersChangedNotConflictious() { }
}
