## ip-counter

[![Maven](https://github.com/unrealwork/ip-counter/actions/workflows/maven.yml/badge.svg)](https://github.com/unrealwork/ip-counter/actions/workflows/maven.yml)

Count unique IPV4 addresses in provided file according
to [task](https://github.com/Ecwid/new-job/blob/master/IP-Addr-Counter.md)

### Usage

App provided as executable jar with main class [IpCounter](src/main/java/com/ecwid/dev/ipcounter/IpCounter.java).

#### Example of usage

```shell
mvn package
java -jar target/ip-counter.jar target/test-classes/ips15.in 
```

### Performance

The main task of this implementation is to maximize utilization of provided resources.

#### Key improvements

- [x] Avoid String creation using reading file by byte and composing integer value corresponding to IP.
- [x] Provide special [data structure](src/main/java/com/ecwid/dev/ipcounter/intset/BigIntSet.java) to store set
  of `int` values efficiently. It requires $2^{32}$ bits to store all possible ips which is around `512Mb` of heap.
- [x] Support for concurrent access to the data structure
  using [AtomicIntegerArray](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/concurrent/atomic/AtomicIntegerArray.html)
- [x] Concurrent processing of file: file is split to independent parts and processed in parallel if possible. The
  approach is implemented using
  reactive [`Flow`](https://docs.oracle.com/javase/9/docs/api/java/util/concurrent/Flow.html) API.
- [x] 
  Using [`MappedByteBuffer`](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/nio/MappedByteBuffer.html)
  for faster reading parts of file.

### Performance result

Measurements are performed on low-middle laptop with following characteristics:

|   |                                                            |
|---|------------------------------------------------------------|
| **Processor**    | AMD Ryzen 7 3700U with Radeon Vega Mobile Gfx     2.30 GHz |
| **RAM** | 16.0 GB (14.9 GB usable)                                   |
| **SSD** | NVMe____WDC_PC_SN530_SDB7                                  |
| **OS** | Windows 11 (ver. 22H2)                                     |
| **JDK**  | graalvm-ce-java17@22.3.1 |

#### Example of measurements

Handling of provided file with size of `120GB` takes about `2 min` in average.

```shell
PS C:\Users\unrea> Measure-Command { java -jar C:\Users\unrea\IdeaProjects\ip-counter\target\ip-counter.jar D:\ip_addresses\ip_addresses | Out-Host }
1000000000


Days              : 0
Hours             : 0
Minutes           : 2
Seconds           : 11
Milliseconds      : 99
Ticks             : 1310990407
TotalDays         : 0.00151735000810185
TotalHours        : 0.0364164001944444
TotalMinutes      : 2.18498401166667
TotalSeconds      : 131.0990407
TotalMilliseconds : 131099.0407
```


