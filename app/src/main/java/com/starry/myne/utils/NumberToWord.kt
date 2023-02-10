package com.starry.myne.utils

import java.text.DecimalFormat


object NumberToWord {
    // string type array for one digit numbers
    private val twoDigits = arrayOf(
        "",
        " Ten",
        " Twenty",
        " Thirty",
        " Forty",
        " Fifty",
        " Sixty",
        " Seventy",
        " Eighty",
        " Ninety"
    )

    // string type array for two digits numbers
    private val oneDigit = arrayOf(
        "",
        " One",
        " Two",
        " Three",
        " Four",
        " Five",
        " Six",
        " Seven",
        " Eight",
        " Nine",
        " Ten",
        " Eleven",
        " Twelve",
        " Thirteen",
        " Fourteen",
        " Fifteen",
        " Sixteen",
        " Seventeen",
        " Eighteen",
        " Nineteen"
    )

    // user-defined method that converts a number to words (up to 1000)
    private fun convertUpToThousand(number: Int): String {
        var number = number
        var soFar: String
        if (number % 100 < 20) {
            soFar = oneDigit[number % 100]
            number /= 100
        } else {
            soFar = oneDigit[number % 10]
            number /= 10
            soFar = twoDigits[number % 10] + soFar
            number /= 10
        }
        return if (number == 0) soFar else oneDigit[number] + " Hundred " + soFar
    }

    // user-defined method that converts a long number (0 to 999999999) to string
    fun convertNumberToWord(number: Long): String {
        // checks whether the number is zero or not
        if (number == 0L) {
            // if the given number is zero it returns zero
            return "zero"
        }
        // the toString() method returns a String object that represents the specified long
        val num: String
        // for creating a mask padding with "0"
        val pattern = "000000000000"
        // creates a DecimalFormat using the specified pattern and also provides the symbols for the default locale
        val decimalFormat = DecimalFormat(pattern)
        // format a number of the DecimalFormat instance
        num = decimalFormat.format(number)

        // the subString() method returns a new string that is a substring of this string
        // the substring begins at the specified beginIndex and extends to the character at index endIndex - 1
        // the parseInt() method converts the string into integer
        val billions = num.substring(0, 3).toInt()
        val millions = num.substring(3, 6).toInt()
        val hundredThousands = num.substring(6, 9).toInt()
        val thousands = num.substring(9, 12).toInt()
        val tradBillions: String = when (billions) {
            0 -> ""
            1 -> convertUpToThousand(billions) + " Billion "
            else -> convertUpToThousand(billions) + " Billion "
        }
        var result = tradBillions
        val tradMillions: String = when (millions) {
            0 -> ""
            1 -> convertUpToThousand(millions) + " Million "
            else -> convertUpToThousand(millions) + " Million "
        }
        result += tradMillions
        val tradHundredThousands: String = when (hundredThousands) {
            0 -> ""
            1 -> "One Thousand "
            else -> convertUpToThousand(hundredThousands) + " Thousand "
        }
        result += tradHundredThousands
        val tradThousand: String = convertUpToThousand(thousands)
        result += tradThousand
        // removing extra space if any
        return result.replace("^\\s+".toRegex(), "").replace("\\b\\s{2,}\\b".toRegex(), " ")
    }
}