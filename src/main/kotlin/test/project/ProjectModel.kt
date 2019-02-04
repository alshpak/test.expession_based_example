package test.project

import java.time.Instant

class ProjectDto {
    val phases: List<PhaseDto> = ArrayList()

}

class PhaseDto {
    val elements: List<ElementDto> = ArrayList()
    var complete: Int? = null
}

class ElementDto {
    val workitems: List<WorkitemDto> = ArrayList()
    var startDate: Instant? = null
    var endDate: Instant? = null
    var complete: Int? = null
}

class WorkitemDto {
    var description: String = ""
    var status: String = ""
}