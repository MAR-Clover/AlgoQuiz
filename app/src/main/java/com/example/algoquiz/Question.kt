package com.example.algoquiz

class Question (val question: String, val example:String, val id:Int, val difficulty:String){
    override fun toString(): String {
        return question // This returns only the question text
    }
}

class QuestionBank{

    companion object {

        // Define an array of Question objects inside the companion object
        val questions = arrayOf(
            Question(
                "Given an integer array nums, return true if any value appears more than once in the array, otherwise return false.",
                "Example 1: Input: nums = [1, 2, 3, 3], Output: true",
                0,
                "Easy"
            ),
            Question(
                "Given a string s, return the length of the longest substring without repeating characters.",
                "Example 1: Input: s = \\\"abcabcbb\\\", Output: 3",
                1,
                "Easy"
            ),
            Question(
                "Given two sorted integer arrays nums1 and nums2, merge nums2 into nums1 as one sorted array.",
                "Example 1: Input: nums1 = [1,2,3,0,0,0], nums2 = [2,5,6] Output: [1,2,2,3,5,6]",
                2,
                "Easy"
            ),
            Question(
                "Given two strings s and t, return true if the two strings are anagrams of each other, otherwise return false.\\nAn anagram is a string that contains the exact same characters as another string, but the order of the characters can be different.",
                "Input: s = racecar, t=carrace, Output: true",
                3,
                "Easy"
            ),
            Question(
                "Given an array of integers nums and an integer target, return the indices i and j such that nums[i] + nums[j] == target and i != j.\\nYou may assume that every input has exactly one pair of indices i and j that satisfy the condition. Return the answer with the smaller index first.",
                "Input: nums = [3,4,5,6], target = 7, Output: [0,1]",
                4,
                "Easy"
            ),
            Question(
                "Given an array of strings strs, group all anagrams together into sublists. You may return the output in any order. An anagram is a string that contains the exact same characters as another string, but the order of the characters can be different.",
                "Input: strs = [\\\"act\\\",\\\"pots\\\",\\\"tops\\\",\\\"cat\\\",\\\"stop\\\",\\\"hat\\\"], Output: [[\\\"hat\\\"],[\\\"act\\\", \\\"cat\\\"],[\\\"stop\\\", \\\"pots\\\", \\\"tops\\\"]]",
                5,
                "Medium"
            ),
            Question(
                "Given an integer array nums and an integer k, return the k most frequent elements within the array. The test cases are generated such that the answer is always unique. You may return the output in any order.",
                "Input: nums = [1,2,2,3,3,3], k = 2, Output: [2,3]",
                6,
                "Medium"
            ),
            Question(
                "Given an array of integers nums, return the length of the longest consecutive sequence of elements that can be formed. A consecutive sequence is a sequence of elements in which each element is exactly 1 greater than the previous element. The elements do not have to be consecutive in the original array. You must write an algorithm that runs in O(n) time.",
                "Input: nums = [2,20,4,10,3,4,5], Output: 4",
                7,
                "Medium"
            )
        )

        fun displayQuestions(): Array<Question> {

            return questions
        }
    }

}