package com.example.smart_city.model

object DummyData {
    val reports = listOf(
        // Cluster padat → merah
        CityReport("report_001", "Jalan Berlubang Parah", "Kerusakan Jalan",
            3.5952, 98.6722, 50, 8,
            "https://picsum.photos/seed/road1/300/200",
            "Jl. Jend. Sudirman, Medan", "2 menit lalu"),
        CityReport("report_002", "Aspal Retak", "Kerusakan Jalan",
            3.5955, 98.6725, 30, 6,
            "https://picsum.photos/seed/road2/300/200",
            "Jl. Jend. Sudirman No.12", "5 menit lalu"),
        CityReport("report_003", "Lubang Besar", "Kerusakan Jalan",
            3.5949, 98.6720, 40, 7,
            "https://picsum.photos/seed/road3/300/200",
            "Jl. Jend. Sudirman No.8", "10 menit lalu"),
        CityReport("report_004", "Kemacetan Panjang", "Kemacetan",
            3.5948, 98.6718, 25, 5,
            "https://picsum.photos/seed/traffic1/300/200",
            "Simpang Sudirman-Imam Bonjol", "15 menit lalu"),
        CityReport("report_005", "Arus Tidak Lancar", "Kemacetan",
            3.5960, 98.6730, 20, 4,
            "https://picsum.photos/seed/traffic2/300/200",
            "Jl. Imam Bonjol, Medan", "12 menit lalu"),
        CityReport("report_006", "Macet Total", "Kemacetan",
            3.5945, 98.6715, 35, 9,
            "https://picsum.photos/seed/traffic3/300/200",
            "Persimpangan Merdeka", "3 menit lalu"),

        // Cluster sedang → kuning
        CityReport("report_007", "Sampah Menumpuk", "Sampah",
            3.5800, 98.6650, 45, 6,
            "https://picsum.photos/seed/trash1/300/200",
            "Jl. Gatot Subroto, Medan", "30 menit lalu"),
        CityReport("report_008", "TPS Overload", "Sampah",
            3.5805, 98.6655, 30, 5,
            "https://picsum.photos/seed/trash2/300/200",
            "Jl. Gatot Subroto No.45", "1 jam lalu"),
        CityReport("report_009", "Banjir Kecil", "Banjir",
            3.5810, 98.6660, 55, 7,
            "https://picsum.photos/seed/flood1/300/200",
            "Jl. Gatot Subroto Km.3", "45 menit lalu"),

        // Titik tunggal → hijau
        CityReport("report_010", "Lampu Jalan Mati", "Listrik",
            3.6100, 98.6900, 15, 3,
            "https://picsum.photos/seed/lamp1/300/200",
            "Jl. Karya, Medan Barat", "2 jam lalu"),
        CityReport("report_011", "Drainase Tersumbat", "Drainase",
            3.5700, 98.6500, 20, 4,
            "https://picsum.photos/seed/drain1/300/200",
            "Jl. Brigjen Katamso", "1 jam lalu"),
        CityReport("report_012", "PKL Liar", "Ketertiban",
            3.6050, 98.6800, 10, 2,
            "https://picsum.photos/seed/peddler1/300/200",
            "Jl. Asia, Medan Kota", "3 jam lalu")
    )
}