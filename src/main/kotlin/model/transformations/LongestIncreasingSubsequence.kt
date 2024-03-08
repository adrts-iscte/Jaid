package model.transformations

import com.github.javaparser.ast.Node
import model.UUID
import model.uuid

fun transform(classBaseMembers : List<Node>, classBranchMembers: List<Node>) : MutableMap<UUID, Int> {
    val classBaseMembersUUIDs = classBaseMembers.map { it.uuid }
    val classBranchMembersUUIDs = classBranchMembers.map { it.uuid }

    val positions = classBaseMembersUUIDs.associateWith { classBranchMembersUUIDs.indexOf(it) }
    val lis = longestIncreasingSubsequence(positions)
    val included = lis.associateWith { true }
    val uuidToIndex = mutableMapOf<UUID, Int>()
    for (i in classBaseMembersUUIDs.lastIndex downTo  0) {
        val elemUUID = classBranchMembersUUIDs[i]
        if (elemUUID !in included) {
            uuidToIndex[elemUUID] = i
        }
    }
    return uuidToIndex
}

fun <Any : kotlin.Any> longestIncreasingSubsequence(positions: Map<Any, Int>): List<Any?> {
    val invertedMapOfPositions = positions.entries.associateBy({ it.value }) { it.key }
    val sequence = positions.values.toList()

    val parent = IntArray(sequence.size)
    //Track the predecessors/parents of elements of each subsequence.
    val increasingSub = IntArray(sequence.size + 1)
    //Track ends of each increasing subsequence.
    var length = 0
    //Length of longest subsequence
    for (i in sequence.indices) {
        //Binary Search
        var low = 1
        var high = length
        while (low <= high) {
            val mid = Math.ceil(((low + high) / 2).toDouble()).toInt()
            if (sequence[increasingSub[mid]] < sequence[i]) {
                low = mid + 1
            } else {
                high = mid - 1
            }
        }
        val pos = low
        //update parent/previous element for LIS
        parent[i] = increasingSub[pos - 1]
        //Replace or append
        increasingSub[pos] = i

        //update the length of the longest subsequence
        if (pos > length) length = pos
    }

    //Generate LIS by travering parent array
    val lis = IntArray(length)
    var k = increasingSub[length]
    for (j in length - 1 downTo 0) {
        lis[j] = sequence[k]
        k = parent[k]
    }

    val lisIndices = lis.toMutableList().subList(0, length)
    return lisIndices.map { invertedMapOfPositions[it] }
}

fun main() {
    val a = mutableListOf('D', 'A', 'B', 'E', 'C')
    val s = mutableListOf('E', 'A', 'B', 'C', 'D')

    transform1(a,s)
}

fun <Any : kotlin.Any> transform1(a : MutableList<Any>, s: MutableList<Any>) {
    val positions = a.associateWith { s.indexOf(it) }
    val lis = longestIncreasingSubsequence(positions)
    println(lis)
    val included = lis.associateWith { true }

    println(a)
    for (i in s.lastIndex downTo  0) {
        val elem = s[i]
        if (elem !in included) {
            a.remove(elem)
            if (s.indexOf(elem) == s.lastIndex) {
                println("Move $elem at the end")
                a.add(elem)
            } else {
                println("Move " + elem + " before " + s[i + 1])
                a.add(i, elem)
            }
        }
    }
    println(a)
}

//fun <Any : kotlin.Any> longestIncreasingSubsequence1(positions: Map<Any, Int>): List<Any?> {
//    val invertedMapOfPositions = positions.entries.associateBy({ it.value }) { it.key }
//    val sequence = positions.values.toList()
//
//    if (sequence.isEmpty())
//        return mutableListOf()
//
//    val tail = IntArray(sequence.size)
//    var length = 1
//    tail[0] = sequence[0]
//    for (i in 1 until sequence.size) {
//        if (sequence[i] > tail[length - 1]) {
//            tail[length++] = sequence[i]
//        } else {
//            var idx = Arrays.binarySearch(tail, 0, length - 1, sequence[i])
//            if (idx < 0) idx = -1 * idx - 1
//            tail[idx] = sequence[i]
//        }
//    }
//    val lisIndices = tail.toMutableList().subList(0, length)
//    return lisIndices.map { invertedMapOfPositions[it] }
//}
//
//fun <Any : kotlin.Any> longestIncreasingSubsequence(positions: Map<Any, Int>): List<Any?> {
//    val invertedMapOfPositions = positions.entries.associateBy({ it.value }) { it.key }
//    val sequence = positions.values.toList()
//
//    if (sequence.isEmpty())
//        return mutableListOf()
//
//    val tail = IntArray(sequence.size)
//    var length = 1
//    tail[0] = sequence[0]
//    for (i in 1 until sequence.size) {
//        if (sequence[i] > tail[length - 1]) {
//            tail[length++] = sequence[i]
//        } else {
//            var idx = Arrays.binarySearch(tail, 0, length - 1, sequence[i])
//            if (idx < 0) idx = -1 * idx - 1
//            tail[idx] = sequence[i]
//        }
//    }
//    val lisIndices = tail.toMutableList().subList(0, length)
//    return lisIndices.map { invertedMapOfPositions[it] }
//}
