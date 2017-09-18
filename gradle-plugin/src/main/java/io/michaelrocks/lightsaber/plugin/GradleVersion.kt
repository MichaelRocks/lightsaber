/*
 * Copyright 2017 Michael Rozumyanskiy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.michaelrocks.lightsaber.plugin

import java.io.Serializable
import java.util.Arrays
import java.util.regex.Pattern

/**
 * Supports versions in the given formats:
 * - major (e.g. 1)
 * - major.minor (e.g. 1.0)
 * - major.minor.micro (e.g. 1.1.1)
 *
 * A version can also be a "preview" (e.g. 1-alpha1, 1.0.0-rc2)
 * or an unreleased version (or "snapshot") (e.g. 1-SNAPSHOT, 1.0.0-alpha1-SNAPSHOT).
 */
class GradleVersion private constructor(
    val rawVersion: String,
    val majorSegment: VersionSegment,
    val minorSegment: VersionSegment?,
    val microSegment: VersionSegment?,
    val additionalSegments: List<VersionSegment>,
    val qualifiersSegment: QualifiersSegment
) : Comparable<GradleVersion>, Serializable {

  val major: Int get() = valueOf(majorSegment)
  val minor: Int get() = valueOf(minorSegment)
  val micro: Int get() = valueOf(microSegment)

  val previewType: String get() = qualifiersSegment.previewType
  val preview: Int get() = qualifiersSegment.preview
  val snapshot: Boolean get() = qualifiersSegment.snapshot

  operator fun compareTo(other: String): Int {
    return compareTo(parse(other))
  }

  override fun compareTo(other: GradleVersion): Int {
    return compareTo(other, false)
  }

  fun compareIgnoringQualifiers(other: String): Int {
    return compareIgnoringQualifiers(parse(other))
  }

  fun compareIgnoringQualifiers(other: GradleVersion): Int {
    return compareTo(other, true)
  }

  fun isAtLeast(
      major: Int,
      minor: Int,
      micro: Int,
      previewType: String = "",
      preview: Int = 0,
      isSnapshot: Boolean = false
  ): Boolean {
    return compareTo(major, minor, micro, previewType, preview, isSnapshot) >= 0
  }

  private fun compareTo(other: GradleVersion, ignoreQualifiers: Boolean): Int {
    return compareTo(
        other.major,
        other.minor,
        other.micro,
        other.previewType,
        other.preview,
        other.snapshot,
        ignoreQualifiers
    )
  }

  private fun compareTo(
      major: Int,
      minor: Int,
      micro: Int,
      previewType: String = "",
      preview: Int = 0,
      isSnapshot: Boolean = false,
      ignoreQualifiers: Boolean = false
  ): Int {
    var delta: Int
    delta = this.major - major
    if (delta != 0) {
      return delta
    }
    delta = this.minor - minor
    if (delta != 0) {
      return delta
    }
    delta = this.micro - micro
    if (delta != 0) {
      return delta
    }

    if (!ignoreQualifiers) {
      delta = this.previewType.compareTo(previewType, ignoreCase = true)
      if (delta != 0) {
        return delta
      }

      delta = this.preview - preview
      if (delta != 0) {
        return delta
      }

      return if (this.snapshot == isSnapshot) 0 else if (this.snapshot) -1 else 1
    }

    return 0
  }


  override fun equals(other: Any?): Boolean {
    return this === other || (other is GradleVersion && compareTo(other) == 0)
  }

  override fun hashCode(): Int {
    return hashCode(majorSegment, minorSegment, microSegment, preview, previewType, snapshot)
  }

  override fun toString(): String {
    return rawVersion
  }

  class VersionSegment private constructor(
      val text: String,
      val value: Int
  ) {
    fun acceptsGreaterValue(): Boolean {
      return PLUS == text
    }

    override fun equals(other: Any?): Boolean {
      return this === other || (other is VersionSegment && text == other.text)
    }

    override fun hashCode(): Int {
      return hashCode(text)
    }

    override fun toString(): String {
      return text
    }

    companion object {
      private val PLUS_SEGMENT = VersionSegment(PLUS, Int.MAX_VALUE)

      fun plus(): VersionSegment {
        return PLUS_SEGMENT
      }

      fun create(value: Int): VersionSegment {
        return VersionSegment(value.toString(), value)
      }

      fun parse(text: String): VersionSegment {
        val value =
            if (text == PLUS) {
              return PLUS_SEGMENT
            } else if (text.startsWith(PLUS)) {
              // +1 is a valid number which will be parsed correctly but it is not a correct version segment.
              throw NumberFormatException("Version segment cannot start with +")
            } else {
              text.toIntOrNull() ?: 0
            }
        return VersionSegment(text, value)
      }
    }
  }

  class QualifiersSegment private constructor(
      val qualifiers: String,
      val previewType: String,
      val preview: Int,
      val snapshot: Boolean
  ) {
    override fun equals(other: Any?): Boolean {
      return this === other || (other is QualifiersSegment && qualifiers == other.qualifiers)
    }

    override fun hashCode(): Int {
      return hashCode(qualifiers)
    }

    override fun toString(): String {
      return qualifiers
    }

    companion object {
      private val EMPTY = QualifiersSegment("", "", 0, false)

      fun empty(): QualifiersSegment {
        return EMPTY
      }

      fun create(snapshot: Boolean = false): QualifiersSegment {
        val qualifiers = if (snapshot) SNAPSHOT else ""
        return QualifiersSegment(qualifiers, "", 0, snapshot)
      }

      fun create(previewType: String, preview: Int, snapshot: Boolean = false): QualifiersSegment {
        require(previewType.isNotEmpty())
        val qualifiers = buildString {
          append(previewType)
          append(preview)
          if (snapshot) {
            append(DASH)
            append(SNAPSHOT)
          }
        }
        return QualifiersSegment(qualifiers, previewType, preview, snapshot)
      }

      fun parse(qualifiers: String): QualifiersSegment {
        if (isSnapshotQualifier(qualifiers)) {
          return QualifiersSegment(qualifiers, "", 0, true)
        }

        // Find and remove "SNAPSHOT" at the end of the qualifiers.
        val lastDashIndex = qualifiers.lastIndexOf(DASH)
        val previewQualifiers = if (lastDashIndex == -1) qualifiers else qualifiers.substring(0, lastDashIndex)
        val snapshot = isSnapshotQualifier(qualifiers.substring(lastDashIndex + 1))
        val previewType: String
        val preview: Int
        val matcher = PREVIEW_PATTERN.matcher(previewQualifiers)
        if (matcher.matches()) {
          previewType = matcher.group(1) ?: ""
          preview = matcher.group(2)?.toIntOrNull() ?: 0
        } else {
          previewType = ""
          preview = 0
        }

        return QualifiersSegment(qualifiers, previewType, preview, snapshot)
      }
    }
  }

  companion object {
    private const val PLUS = "+"
    private const val DASH = "-"
    private const val SNAPSHOT = "SNAPSHOT"
    private const val DEV = "dev"

    private val PREVIEW_PATTERN = Pattern.compile("([a-zA-Z]+)[\\-]?([\\d]+)?")

    fun create(major: Int, minor: Int, micro: Int): GradleVersion {
      return GradleVersion(
          major.toString() + "." + minor + "." + micro,
          VersionSegment.create(major),
          VersionSegment.create(minor),
          VersionSegment.create(micro),
          emptyList(),
          QualifiersSegment.empty()
      )
    }

    fun tryParse(value: String): GradleVersion? {
      return try {
        parse(value)
      } catch (exception: RuntimeException) {
        null
      }
    }

    fun parse(value: String): GradleVersion {
      val dashIndex = value.indexOf(DASH)
      val version: String
      val qualifiers: String
      if (dashIndex != -1) {
        version = value.substring(0, dashIndex)
        qualifiers = value.substring(dashIndex + 1)
      } else {
        version = value
        qualifiers = ""
      }

      try {
        val parsedVersionSegments = splitSegments(version)
        val segmentCount = parsedVersionSegments.size
        if (segmentCount > 0) {
          val majorSegment = parsedVersionSegments[0]
          val minorSegment = parsedVersionSegments.getOrNull(1)
          val microSegment = parsedVersionSegments.getOrNull(2)
          val additionalSegments = parsedVersionSegments.drop(3)
          val qualifiersSegment = QualifiersSegment.parse(qualifiers)
          return GradleVersion(
              value,
              majorSegment,
              minorSegment,
              microSegment,
              additionalSegments,
              qualifiersSegment
          )
        }
      } catch (exception: NumberFormatException) {
        parsingFailure(value, exception)
      }

      parsingFailure(value)
    }

    private fun splitSegments(version: String): List<VersionSegment> {
      return version.split('.').flatMap { parseSegment(it) }
    }

    private fun parseSegment(text: String): List<VersionSegment> {
      val length = text.length
      if (length > 1 && text.endsWith(PLUS)) {
        // Segment has a number and a '+' (e.g. second segment in '2.1+').
        // We need to split '1+' into 2 segments: '1' and '+'.
        val segments = listOf(
            VersionSegment.parse(text.substring(0, length - 1)),
            VersionSegment.plus()
        )
        return segments
      }

      return listOf(VersionSegment.parse(text))
    }

    private fun isSnapshotQualifier(value: String): Boolean {
      return SNAPSHOT.equals(value, ignoreCase = true) || DEV.equals(value, ignoreCase = true)
    }

    private fun parsingFailure(value: String, cause: Throwable? = null): Nothing {
      throw IllegalArgumentException("'$value' is not a valid version", cause)
    }

    private fun valueOf(segment: VersionSegment?): Int {
      return segment?.value ?: 0
    }

    private fun hashCode(vararg objects: Any?): Int {
      return Arrays.hashCode(objects)
    }
  }
}
