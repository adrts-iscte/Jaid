//2a55a277-4941-40e5-876f-0cb7a8abaec0
package scenarios.detachRedundantTransformations.fileRedundantTransformations.branchToBeMerged;

import java.util.List;

//76b74b0a-d31f-4fbc-a837-048474fd7771
interface Class {

}

//765cc63d-6083-4da2-aeb2-91c94a0fb3f1
class ToBeAdded {

    //f5532070-524a-44b5-aa57-f70ddfe4e5ec
    double attribute;

    //a6c571f1-60ee-4733-99af-b358b8f8d0db
    static void method() {}
}

//3fd23723-5be9-4369-ba1d-35c8107eafc6
final class Renamed extends ToBeAdded implements Class {

    //1a878baf-9ca3-4c9b-83d5-a4269f07168f
    void RenamedMethod() {
        ToBeAdded.method();
    }

}
