package io.github.jd1378.otphelper.utils

class CodeExtractor {
  companion object {
    private val sensitiveWords =
        listOf(
            "code",
            "کد",
            "رمز",
            "\\bOTP\\W",
            "\\b2FA\\W",
            "Einmalkennwort",
            "contraseña",
            "c[oó]digo",
            "clave",
            "验证码",
            "校验码",
            "識別碼",
            "認證",
            "驗證",
            "код",
            "סיסמ",
            "קוד",
            "\\bKodu\\b",
            "\\bKodunuz\\b",
            "\\bKodi\\b",
            "\\bKods\\b",
            "\\bTAN\\b",
            "\\bmTAN\\b",
            "\\bmã\\W", // "code" in vietnamese
            "\\bcodice\\W", // "code" in italian
        )

    private val ignoredWords =
        listOf(
            "مقدار",
            "مبلغ",
            "amount",
            "برای",
            "-ارز",
            // avoids detecting space separated code as bunch of words:
            "[a-zA-Z0-9] [a-zA-Z0-9] [a-zA-Z0-9] [a-zA-Z0-9] ?",
        )

    private val generalCodeMatcher =
        """(?:${sensitiveWords.joinToString("|")})(?:\s*(?!${
                ignoredWords.joinToString("|")
            })[^\s:.'"\d\u0660-\u0669\u06F0-\u06F9])*:?\s*(["']?)${""
              // this comment is to separate parts
          }([\d\u0660-\u0669\u06F0-\u06F9a-zA-Z]{4,}|(?: [\d\u0660-\u0669\u06F0-\u06F9a-zA-Z]){4,}|)\1(?:[^\d\u0660-\u0669\u06F0-\u06F9a-zA-Z]|${'$'})"""
            .toRegex(
                setOf(
                    RegexOption.IGNORE_CASE,
                    RegexOption.MULTILINE,
                ))

    private val specialCodeMatcher =
        """([\d\u0660-\u0669\u06F0-\u06F9 ]{4,}(?=\s)).*(?:${sensitiveWords.joinToString("|")})"""
            .toRegex(setOf(RegexOption.IGNORE_CASE, RegexOption.MULTILINE))

    fun getCode(str: String): String? {
      val results = generalCodeMatcher.findAll(str).filter { it.groups[2]?.value != null }
      if (results.count() > 0) {
        // generalCodeMatcher also detects if the text contains "code" keyword
        // so we only run google's regex only if general regex did not capture the "code" group
        val foundCode =
            results
                .find { it.groups[2]!!.value.isNotEmpty() }
                ?.groups
                ?.get(2)
                ?.value
                ?.replace(" ", "")
        if (foundCode !== null) {
          return toEnglishNumbers(foundCode)
        }
        return toEnglishNumbers(
            specialCodeMatcher.find(str)?.groups?.get(1)?.value?.replace(" ", ""))
      }
      return null
    }

    private fun toEnglishNumbers(number: String?): String? {
      if (number.isNullOrEmpty()) return null
      val chars = CharArray(number.length)
      for (i in number.indices) {
        var ch = number[i]
        if (ch.code in 0x0660..0x0669) {
          ch -= (0x0660 - '0'.code)
        } else if (ch.code in 0x06f0..0x06F9) {
          ch -= (0x06f0 - '0'.code)
        }
        chars[i] = ch
      }
      return String(chars)
    }
  }
}
