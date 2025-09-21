SUMMARY = "A cross-platform benchmark for Android, iOS, Linux, MacOS, and Windows"
HOMEPAGE = "https://geekbench.com"
LICENSE = "geekbench6-eula"

# EULA is not contained within the tarball. License text copied from
# https://www.primatelabs.com/legal/eula-v6.html
NO_GENERIC_LICENSE[geekbench6-eula] = "geekbench6-eula"
LIC_FILES_CHKSUM = "file://geekbench6-eula;md5=458fdf315b32b871076a2a8909e450c4"
LICENSE_FLAGS = "commercial"

COMPATIBLE_HOST = "(aarch64|riscv64|x86_64).*-linux"

GEEKBENCH_ARCH:aarch64 = "ARMPreview"
GEEKBENCH_ARCH:riscv64 = "RISCVPreview"
GEEKBENCH_ARCH:x86-64 = ""

GEEKBENCH_SHA:aarch64 = "e6a24ad0071a33b3c280d378fccc09a125c7492754f6010ca79de4758e26aab1"
GEEKBENCH_SHA:riscv64 = "b544805046af45c2429efc36f9a3f19ec7aad14f276c67ffac8255109c4f574e"
GEEKBENCH_SHA:x86-64 = "c7156003f3fe7aedc986af7b02fbf654de8ab5e9ff530cab17c088ad3c2b0538"

SRC_URI = " \
    https://cdn.geekbench.com/Geekbench-${PV}-Linux${GEEKBENCH_ARCH}.tar.gz \
    file://geekbench6-eula \
"
SRC_URI[sha256sum] = "${GEEKBENCH_SHA}"

S = "${WORKDIR}/Geekbench-${PV}-Linux${GEEKBENCH_ARCH}"

# Disable stuff not needed for packaging binaries
INHIBIT_PACKAGE_STRIP = "1"
INHIBIT_PACKAGE_DEBUG_SPLIT = "1"
do_patch[noexec] = "1"
do_configure[noexec] = "1"
do_compile[noexec] = "1"

# Place license file so NO_GENERIC_LICENSE works
do_place_license() {
    cp ${WORKDIR}/geekbench6-eula ${S}
}
addtask place_license after do_unpack before do_patch

do_install() {
    install -d ${D}/opt/${BPN}
    install -m 755 ${S}/geekbench6 ${D}/opt/${BPN}
    install -m 644 ${S}/geekbench.plar ${D}/opt/${BPN}
    install -m 644 ${S}/geekbench-workload.plar ${D}/opt/${BPN}

    install -d ${D}${bindir}
    ln -s /opt/${BPN}/geekbench6 ${D}${bindir}/geekbench6
}

do_install:append:aarch64() {
    install -m 755 ${S}/geekbench_aarch64 ${D}/opt/${BPN}
}

do_install:append:riscv64() {
    install -m 755 ${S}/geekbench_riscv64 ${D}/opt/${BPN}
}

do_install:append:x86-64() {
    install -m 755 ${S}/geekbench_avx2 ${D}/opt/${BPN}
    install -m 755 ${S}/geekbench_x86_64 ${D}/opt/${BPN}

    # The x86-64 binaries assume libraries are in /lib64, but a typical pure
    # 64-bit image puts them in /lib. If not building a multilib image, add a
    # symlink from /lib64 to /lib.
    if [ -z "${MULTILIBS}" ]; then
        ln -s /lib ${D}/lib64
    fi
}

FILES:${PN} = " \
    ${bindir} \
    /opt/${BPN} \
"
FILES:${PN}:append:x86-64 = " /lib64"

# Ignore warning about relocations in .text
INSANE_SKIP:${PN} += "textrel"
