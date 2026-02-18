# Tucil1_13524050

## Deskripsi Singkat

Program ini varian *Queens* puzzle dari linkedin — menggunakan algoritma **Brute Force**.  
Aplikasi menyediakan GUI berbasis JavaFX dengan tampilan papan interaktif, dua mode penyelesaian, serta fitur ekspor solusi ke PNG dan TXT.

## Fitur

| Fitur | Keterangan |
|---|---|
| **Load** | Memuat konfigurasi papan dari file `.txt` |
| **Solve — Pure** | Brute force murni ($\binom{n^2}{n}$ kombinasi) |
| **Solve — Constraints** | Brute force dengan pruning (satu queen per baris, kolom, diagonal, dan region) |
| **Save PNG** | Menyimpan screenshot papan solusi ke file gambar |
| **Save TXT** | Menyimpan solusi ke file teks |
| **Cancel** | Klik tombol `:)` untuk membatalkan pencarian |

## Struktur Repository

```
.
├── data/
├── doc/
├── lib/
├── src/
│   ├── App.java
│   ├── Cell.java
│   ├── Config.java
│   ├── Launcher.java
│   ├── Reader.java
│   └── Solver.java
├── test/
├── pom.xml
├── run.bat          # Untuk compile & run (Windows)
└── README.md
```

## Requirement

- **Java JDK 17** atau lebih baru
- **Apache Maven 3.8+**

Pastikan `java` dan `mvn` sudah tersedia di `PATH`.

## Cara Menjalankan

### Windows

```bat
run.bat
```

### Linux / macOS / WSL

```bash
mvn compile javafx:run
```

### Menjalankan via JAR

Build JAR terlebih dahulu:

```bash
mvn package -q
```

Lalu jalankan:

**Windows:**
```bat
java --module-path lib --add-modules javafx.controls,javafx.swing -jar bin\queensweeper.jar
```

**Linux / WSL:**
```bash
java --module-path lib --add-modules javafx.controls,javafx.swing -jar bin/queensweeper.jar
```

## Format File Konfigurasi

File konfigurasi berupa grid $n \times n$ huruf kapital (`A`–`Z`), di mana setiap huruf merepresentasikan satu region.  
Jumlah region harus sama dengan $n$, dan setiap region harus kontinu (connected).

**Contoh** (`data/5.txt` — grid 5×5):

```
AABBC
AABCC
ADDCC
DDDEC
DEEEC
```

## Algoritma

### Pure Brute Force
Menghasilkan semua kombinasi $\binom{n^2}{n}$ penempatan queen, lalu mengecek setiap kombinasi apakah memenuhi semua constraint (baris, kolom, diagonal, region unik).

### Constrained Brute Force
Untuk setiap region, menempatkan tepat satu queen di salah satu sel region tersebut, lalu memvalidasi constraint secara inkremental. Pencarian di-prune saat terjadi konflik sehingga jauh lebih cepat.

## Author

- **Nama**: Raysha Erviandika Putra
- **NIM**: 13524050
