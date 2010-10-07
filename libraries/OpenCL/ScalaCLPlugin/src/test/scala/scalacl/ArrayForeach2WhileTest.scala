/*
 * ScalaCL - putting Scala on the GPU with JavaCL / OpenCL
 * http://scalacl.googlecode.com/
 *
 * Copyright (c) 2009-2010, Olivier Chafik (http://ochafik.free.fr/)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of Olivier Chafik nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY OLIVIER CHAFIK AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package scalacl

import java.io.File
import org.junit._
import Assert._

class ArrayForeach2WhileTest extends TestUtils {

  @Test
  def simpleStringArrayForeach =
    simpleArrayForeach("String")

  @Test
  def inlineStringArrayByLengthForeach =
    inlineArrayByLengthForeach("String")

  @Test
  def inlineStringArrayWithElementsForeach =
    inlineArrayWithElementsForeach("String", "\"a\", \"b\", \"c\"")

  @Test
  def simplePrimitiveArrayForeach =
    for (p <- Seq("Double", "Float", "Int", "Short", "Long", "Byte", "Char", "Boolean"))
      simpleArrayForeach(p)

  @Test
  def inlinePrimitiveArrayByLengthForeach =
    for (p <- Seq("Double", "Float", "Int", "Short", "Long", "Byte", "Char", "Boolean"))
      inlineArrayByLengthForeach(p)

  @Test
  def inlinePrimitiveArrayWithElementsForeach =
    for (p <- Seq("Double", "Float", "Int", "Short", "Long", "Byte", "Char", "Boolean"))
      inlineArrayWithElementsForeach(p, if (p == "Boolean") "true, true, false" else Array(1, 2, 3).map("(" + _ + ": " + p + ")").mkString(", "))

  def simpleArrayForeach(typeStr: String) {
    ensurePluginCompilesSnippetsToSameByteCode("simple" + typeStr + "ArrayForeach",
      """
          val a = new Array[""" + typeStr + """](10)
          a.foreach(println(_))
      """,
      """
          val a = new Array[""" + typeStr + """](10)
          val aa = a
          val n = aa.length
          var i = 0
          while (i < n)
          {
            val item = aa(i)
            println(item)
            i += 1
          }
      """
    )
  }

  def inlineArrayByLengthForeach(typeStr: String) {
    ensurePluginCompilesSnippetsToSameByteCode("inline" + typeStr + "ArrayForeach",
      """
          new Array[""" + typeStr + """](10).foreach(println(_))
      """,
      """ 
          val aa = new Array[""" + typeStr + """](10)
          val n = aa.length
          var i = 0
          while (i < n)
          {
            val item = aa(i)
            println(item)
            i += 1
          }
      """
    )
  }
  def inlineArrayWithElementsForeach(typeStr: String, itemsStr: String) {
    ensurePluginCompilesSnippetsToSameByteCode("inline" + typeStr + "ArrayWithElementsForeach",
      """
          Array(""" + itemsStr + """).foreach(println(_))
      """,
      """
          val aa = Array(""" + itemsStr + """)
          val n = aa.length
          var i = 0
          while (i < n)
          {
            val item = aa(i)
            println(item)
            i += 1
          }
      """
    )
  }
}
