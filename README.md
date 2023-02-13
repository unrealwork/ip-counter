## ip-counter

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

#### Example of measurements

Reading of provided file with size of `120GB` takes about `6.5 min` in average.

```shell
PS D:\ip_addresses> Measure-Command { java -jar C:\Users\unrea\IdeaProjects\ip-counter\target\ip-counter.jar D:\ip_addresses\ip_addresses | Out-Host }
1000000000


Days              : 0
Hours             : 0
Minutes           : 6
Seconds           : 19
Milliseconds      : 925
Ticks             : 3799251176
TotalDays         : 0.0043972814537037
TotalHours        : 0.105534754888889
TotalMinutes      : 6.33208529333333
TotalSeconds      : 379.9251176
TotalMilliseconds : 379925.1176
```


