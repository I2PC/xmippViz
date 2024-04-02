#***************************************************************************
# Authors:     Oier Lauzirika Zarrabeitia (oierlauzi@bizkaia.eu)
#
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 2 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program; if not, write to the Free Software
# Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
# 02111-1307  USA
#
#  All comments concerning this program package may be sent to the
#  e-mail address 'xmipp@cnb.csic.es'
# ***************************************************************************

cmake_minimum_required(VERSION 3.16)

include(GNUInstallDirs)

# Find dependencies
find_package(Java REQUIRED)
find_package(JNI REQUIRED)
include(UseJava)

function(write_java_enum HEADER_FILE OUTPUT_FILE ENUM_NAME)
	get_filename_component(CLASS_NAME ${OUTPUT_FILE} NAME_WE)

	# Read header file
	file(READ "${HEADER_FILE}" HEADER_CONTENT)

	if ("${HEADER_CONTENT}" MATCHES "enum\\s+${ENUM_NAME}\\s*{\\s*([^{}]+)\\s*}")
		set(ENUM_VALUES ${CMAKE_MATCH_1})
		configure_file(
			${CMAKE_CURRENT_SOURCE_DIR}/cmake/enum.java.in
			${OUTPUT_FILE}
		)
	else()
		message(SEND_ERROR "Could not find requested enum")
	endif()
endfunction()


# Find source files
file(
	GLOB_RECURSE 
	JNI_SOURCES 
		${CMAKE_CURRENT_SOURCE_DIR}/bindings/java/*.cpp 
)
file(
	GLOB_RECURSE 
	JAVA_IJ_SOURCES 
		${CMAKE_CURRENT_SOURCE_DIR}/java/src/xmipp/ij/commons/*.java 
)
file(
	GLOB_RECURSE 
	JAVA_JNI_SOURCES 
		${CMAKE_CURRENT_SOURCE_DIR}/java/src/xmipp/jni/*.java 
)
file(
	GLOB_RECURSE 
	JAVA_TEST_SOURCES 
		${CMAKE_CURRENT_SOURCE_DIR}/java/src/xmipp/test/*.java 
)
file(
	GLOB_RECURSE 
	JAVA_TOMO_SOURCES 
		${CMAKE_CURRENT_SOURCE_DIR}/java/src/xmipp/tomography/*.java 
)
file(
	GLOB_RECURSE 
	JAVA_UTILS_SOURCES 
		${CMAKE_CURRENT_SOURCE_DIR}/java/src/xmipp/utils/*.java 
)
file(
	GLOB_RECURSE 
	JAVA_VIEWER_SOURCES 
		${CMAKE_CURRENT_SOURCE_DIR}/java/src/xmipp/viewer/*.java 
)
set(IJ_LINK ${CMAKE_CURRENT_SOURCE_DIR}/java/lib/ij.jar)

# Create the shared library
add_library(XmippJNI SHARED ${JNI_SOURCES})
#target_precompile_headers(
#	xmippLib
#	PRIVATE
#		${HEADERS}
#)
target_link_libraries(
	XmippJNI
	PUBLIC
		Xmipp
)
target_include_directories(
	XmippJNI
	PRIVATE
		${JNI_INCLUDE_DIRS}
)
install(
	TARGETS XmippJNI
 	LIBRARY DESTINATION ${CMAKE_INSTALL_LIBDIR}
)

# Get required header files
get_target_property(XMIPP_CORE_INCLUDE_DIR XmippCore INTERFACE_INCLUDE_DIRECTORIES)
find_file(METADATA_LABEL_HEADER NAME "core/metadata_label.h" PATHS ${XMIPP_CORE_INCLUDE_DIR} REQUIRED)
find_file(ERROR_HEADER NAME "core/xmipp_error.h" PATHS ${XMIPP_CORE_INCLUDE_DIR} REQUIRED)

# Configure files
write_java_enum(
	"${METADATA_LABEL_HEADER}"
	"${CMAKE_CURRENT_SOURCE_DIR}/java/src/xmipp/jni/MDLabel.java"
	"MDLabel"
)
write_java_enum(
	"${ERROR_HEADER}"
	"${CMAKE_CURRENT_SOURCE_DIR}/java/src/xmipp/jni/XmippError.java"
	"ErrorType"
)

# Create JAVA JNI binding
add_jar(
	XmippJavaJNI
	SOURCES ${JAVA_JNI_SOURCES} ${IJ_LINK}
	OUTPUT_NAME XmippJNI
)

# Create JAVA utility lib
add_jar(
	XmippUtils
	SOURCES ${JAVA_UTILS_SOURCES} ${IJ_LINK}
	INCLUDE_JARS XmippJavaJNI
)

# Create JAVA ImageJ library
add_jar(
	XmippIJ
	SOURCES ${JAVA_IJ_SOURCES}
	INCLUDE_JARS XmippJavaUtils
)

# Create the viewer
add_jar(
	XmippViewer
	SOURCES ${JAVA_VIEWER_SOURCES}
	INCLUDE_JARS XmippIJ
)

# Create the tests
add_jar(
	XmippTest
	SOURCES ${JAVA_TEST_SOURCES}
	INCLUDE_JARS XmippViewer
)

# Install python bindings and resources
install(
	DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}/bindings/python
	DESTINATION ${CMAKE_INSTALL_PREFIX}/bindings/python
)
install(
	DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}/resources
	DESTINATION ${CMAKE_INSTALL_PREFIX}/resources
)

add_subdirectory(applications)
