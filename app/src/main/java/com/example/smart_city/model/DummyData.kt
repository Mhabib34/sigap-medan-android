package com.example.smart_city.model

object DummyData {
    val reports = listOf(
        CityReport("report_01", "Jalan Berlubang", "Kerusakan Jalan",
            3.643889, 98.657531, 15, 4,
            "https://res.cloudinary.com/djvnq6rw8/image/upload/v1777788021/gambar1_aew9ex.jpg",
            "Jl. Veteran", "2 jam lalu"),
        CityReport("report_02", "Jalan Berlubang", "Kerusakan Jalan",
            3.654293, 98.656764, 15, 4,
            "https://res.cloudinary.com/djvnq6rw8/image/upload/v1777788021/gambar2_z6eysf.jpg",
            "Jl. Veteran", "1 jam lalu"),
        CityReport("report_03", "Jalan Berlubang", "Kerusakan Jalan",
            3.657129, 98.656634, 15, 3,
            "https://res.cloudinary.com/djvnq6rw8/image/upload/v1777788021/gambar3_w93onb.jpg",
            "Jl. Veteran", "3 jam lalu"),
        CityReport("report_04", "Jalan Berlubang Parah", "Kerusakan Jalan",
            3.723343, 98.678788, 15, 7,
            "https://res.cloudinary.com/djvnq6rw8/image/upload/v1777788021/gambar4_ojhk30.jpg",
            "Jl. Titi Pahlawan", "1 jam lalu"),
        CityReport("report_05", "Drainase Tersumbat", "Drainase",
            3.723456, 98.678981, 20, 4,
            "https://res.cloudinary.com/djvnq6rw8/image/upload/v1777788021/gambar5_fzt9r7.jpg",
            "Jl. Titi Pahlawan", "1 jam lalu"),
    )
}