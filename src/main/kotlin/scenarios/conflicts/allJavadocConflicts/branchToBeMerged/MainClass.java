//9450ee42-c2ce-4cb7-85d5-ff457c728d81
package scenarios.conflicts.allJavadocConflicts.branchToBeMerged;

/**
 * javadocRemovedAndChanged
 *
 * a6b874f9-aade-4947-a230-9b5cb9d7a295
 */
class MainClass {

    /**
     * bothChangesAreDifferent 1
     *
     * a34a40f2-c5c6-4312-ba2a-76913b1ec866
     */
    int bothChangesAreDifferent;

    /**
     * bothAdditionsAreDifferent 1
     *
     * 373b7467-de3b-4fab-9025-dd2c53b53e10
     */
    void method(){}

    /**
     * newJavadocToTheRemovedMethod
     *
     * 19313f21-6f80-44f6-af3c-462d0f19ead7
     */
    void methodToBeRemoved(){}

    /**
     * newJavadocToTheRemovedField
     *
     * 79e4fe37-aecf-4857-b80c-ab1f1a1d5965
     */
    int fieldToBeRemoved;
}

/**
 * newJavadocToTheRemovedClass
 *
 * e8d2698e-1cd7-4a20-b2dc-793bebcf76da
 */
class classToBeRemoved {}

/**
 * newJavadocToTheRemovedInterface
 *
 * 7cd25ade-43b7-4905-b821-5614bbadee71
 */
interface interfaceToBeRemoved {}

//15be5781-a7ac-498d-b4d7-3b8f9fbcaca9
enum enumToChanged{

    /**
     * Javadoc added
     * 7053c456-cedd-4454-a3df-2181abb89fb9
     */
    ENUM_CONSTANT

}