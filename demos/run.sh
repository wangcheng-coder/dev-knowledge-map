#!/bin/bash
set -euo pipefail
cd "$(dirname "$0")"
rm -rf out extra transcripts
mkdir -p out extra transcripts
javac -encoding UTF-8 -d out src/*.java
javac -encoding UTF-8 -d extra src-extra/LoadedTwice.java
python3 disasm.py out/mapdemo/BytecodeSample.class | tee transcripts/BytecodeSample.javap.txt
run() {
  local name="$1"
  shift
  java -Dfile.encoding=UTF-8 -cp out "mapdemo.$name" "$@" | tee "transcripts/$name.txt"
  echo
}
run PassByValue
run InitOrder
run ClassInitOnce
run GenericsErasure
run EqualsHashCode
run TryWithResources
run BytecodeSample
run LostUpdate
run ThreadPoolReject
run BufferIndices
run FrameDecoder
run ProxySelfInvocation
run MiniContainer
run TwoClassLoaders extra
run PageTableWalk
run MesiSim
run TcpHandshake
run BootChain
run IsolationSim
run WritePath
run RequestPath
run FalseSharing
