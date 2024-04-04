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

	# Parse the enum
	set(ENUM_REGEX "enum[ \t\n]+${ENUM_NAME}[ \t\n]*{[ \t\n]*([^{}]*)[ \t\n]*}")
	if ("${HEADER_CONTENT}" MATCHES ${ENUM_REGEX})
		set(ENUM_BODY ${CMAKE_MATCH_1})
		string(REGEX MATCHALL "[^\n\r]+" ENUM_BODY ${ENUM_BODY})
		
		foreach(ENUM_ITEM ${ENUM_BODY})
			if("${ENUM_ITEM}" MATCHES "([a-zA-Z0-9_]+)")
				set(ENUM_ITEM_NAME ${CMAKE_MATCH_1})

				# Write the item prefix
				string(APPEND ENUM_VALUES "\tpublic static final int ")

				if("${ENUM_ITEM}" MATCHES "^[ \t\n]*([a-zA-Z0-9_]+[ \t\n]*=[ \t\n]*[\\+-]?[a-zA-Z0-9_]+)")
					# Assignment item. Use it as-is
					string(APPEND ENUM_VALUES "${CMAKE_MATCH_1}")
				else()
					# "Normal" item. Increment previous value (or start from zero)
					if(ENUM_LAST_ITEM_NAME)
						string(APPEND ENUM_VALUES "${ENUM_ITEM_NAME} = ${ENUM_LAST_ITEM_NAME}+1")
					else()
						string(APPEND ENUM_VALUES "${ENUM_ITEM_NAME} = 0")
					endif()
				endif()

				# Write the item suffix
				string(APPEND ENUM_VALUES ";\n")

				# Update the last item name
				set(ENUM_LAST_ITEM_NAME ${ENUM_ITEM_NAME})
			else()
				message(WARNING "Unable to parse enum line")
			endif()
		endforeach()
		
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
file(
	GLOB_RECURSE 
	JAVA_JARS 
		${CMAKE_CURRENT_SOURCE_DIR}/java/lib/*.jar
)

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

# Extract the ImageJ JAR
set(IMAGEJ_TAR "${CMAKE_CURRENT_SOURCE_DIR}/external/imagej.tgz")
set(IMAGEJ_JAR "${CMAKE_CURRENT_SOURCE_DIR}/external/imagej/ij.jar")
add_custom_command(
	OUTPUT ${IMAGEJ_JAR}
	COMMAND ${CMAKE_COMMAND} -E tar -xzf ${IMAGEJ_TAR}
	WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}/external/
	DEPENDS ${IMAGEJ_TAR}
	COMMENT "Unpacking ${IMAGEJ_TAR}"
	VERBATIM
)

set(JAVA_LIB_INSTALL_DIR bindings/java/lib)

# Create JAVA JNI binding
add_jar(
	XmippJavaJNI
	SOURCES ${JAVA_JNI_SOURCES}
	OUTPUT_NAME XmippJNI
	INCLUDE_JARS 
		${JAVA_JARS}
		"${IMAGEJ_JAR}"
)
install_jar(
	XmippJavaJNI ${JAVA_LIB_INSTALL_DIR}
)

# Create JAVA utility lib
add_jar(
	XmippJavaUtils
	SOURCES ${JAVA_UTILS_SOURCES}
	OUTPUT_NAME XmippUtils
	INCLUDE_JARS 
		XmippJavaJNI 
		${JAVA_JARS}
		"${IMAGEJ_JAR}"
)
install_jar(
	XmippJavaUtils ${JAVA_LIB_INSTALL_DIR}
)

# Create JAVA ImageJ library
add_jar(
	XmippIJ
	SOURCES ${JAVA_IJ_SOURCES}
	INCLUDE_JARS 
		XmippJavaJNI 
		XmippJavaUtils
		${JAVA_JARS}
		"${IMAGEJ_JAR}"
)
install_jar(
	XmippIJ ${JAVA_LIB_INSTALL_DIR}
)

# Create the viewer
add_jar(
	XmippViewer
	SOURCES ${JAVA_VIEWER_SOURCES}
	ENTRY_POINT xmipp.viewer.Viewer
	INCLUDE_JARS 
		XmippJavaJNI 
		XmippJavaUtils
		XmippIJ
		${JAVA_JARS}
		"${IMAGEJ_JAR}"

)
install_jar(
	XmippViewer ${JAVA_LIB_INSTALL_DIR}
)

# Create the tests
add_jar(
	XmippTest
	SOURCES ${JAVA_TEST_SOURCES}
	INCLUDE_JARS 
		XmippJavaJNI 
		XmippJavaUtils
		XmippIJ
		XmippViewer
		${JAVA_JARS}
		"${IMAGEJ_JAR}"
)
install_jar(
	XmippTest ${JAVA_LIB_INSTALL_DIR}
)

# Install java libraries
install(
	DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}/java/lib/
	DESTINATION ${JAVA_LIB_INSTALL_DIR}
)
install(
	FILES ${IMAGEJ_JAR}
	DESTINATION ${JAVA_LIB_INSTALL_DIR}
)

# Install python bindings and resources
install(
	DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}/bindings/python/
	DESTINATION bindings/python
)
install(
	DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}/resources/
	DESTINATION resources
)

add_subdirectory(applications)
