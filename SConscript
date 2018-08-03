#!/usr/bin/env python

# **************************************************************************
# *
# * Authors:     I. Foche Perez (ifoche@cnb.csic.es)
# *              J.M. de la Rosa Trevin (jmdelarosa@cnb.csic.es)
# *
# * Unidad de Bioinformatica of Centro Nacional de Biotecnologia, CSIC
# *
# * This program is free software; you can redistribute it and/or modify
# * it under the terms of the GNU General Public License as published by
# * the Free Software Foundation; either version 2 of the License, or
# * (at your option) any later version.
# *
# * This program is distributed in the hope that it will be useful,
# * but WITHOUT ANY WARRANTY; without even the implied warranty of
# * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# * GNU General Public License for more details.
# *
# * You should have received a copy of the GNU General Public License
# * along with this program; if not, write to the Free Software
# * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
# * 02111-1307  USA
# *
# *  All comments concerning this program package may be sent to the
# *  e-mail address 'ifoche@cnb.csic.es'
# *
# **************************************************************************

import os
from os.path import join
from glob import glob
from datetime import datetime


Import('env')


# Define some variables used by Scons. Note that some of
# the variables will be passed by Scipion in the environment (env).

get = lambda x: os.environ.get(x, '0').lower() in ['true', 'yes', 'y', '1']

gtest = get('GTEST')
debug = get('DEBUG')

# Read some flags
CYGWIN = env['PLATFORM'] == 'cygwin'
MACOSX = env['PLATFORM'] == 'darwin'
MINGW = env['PLATFORM'] == 'win32'

XMIPP_PATH = Dir('.').abspath
XMIPP_BUNDLE = Dir('..').abspath


#  ***********************************************************************
#  *                      Xmipp C++ Libraries                            *
#  ***********************************************************************

# Create a shortcut and customized function
# to add the Xmipp CPP libraries
def addLib(name, **kwargs):
    # Install all libraries in scipion/software/lib
    # COSS kwargs['installDir'] = '#software/lib'
    # Add always the xmipp path as -I for include and also xmipp/libraries
    incs = kwargs.get('incs', []) + [join(XMIPP_PATH, 'external'),
                                     join(XMIPP_PATH, 'libraries')]
    kwargs['incs'] = incs

    deps = kwargs.get('deps', [])
    kwargs['deps'] = deps

    # Add libraries in libs as deps if not present
    libs = kwargs.get('libs', [])
    for lib in libs:
        deps.append(lib)

    # If pattern not provided use *.cpp as default
    patterns = kwargs.get('patterns', '*.cpp')
    kwargs['patterns'] = patterns
    lib = env.AddCppLibrary(name, **kwargs)
    	
    env.Alias('xmipp-libs', lib)

    return lib


# Java binding
addLib('XmippJNI',
       dirs=['bindings'],
       patterns=['java/*.cpp'],
       incs=[env['JNI_CPPPATH'],"%s/xmippCore"%XMIPP_BUNDLE,"%s/xmipp/libraries"%XMIPP_BUNDLE],
       libs=['pthread', 'XmippCore','Xmipp'],
       libpath=["%s/xmippCore/lib"%XMIPP_BUNDLE,"%s/xmipp/lib"%XMIPP_BUNDLE])


#  ***********************************************************************
#  *                      Java Libraries                                 *
#  ***********************************************************************

# Helper functions so we don't write so much.
fpath = lambda path: File('%s' % path).abspath
dpath = lambda path: Dir('%s' % path).abspath
epath = lambda path: Entry('%s' % path).abspath

javaEnumDict = {
    'ImageWriteMode': [fpath('%s/xmippCore/core/xmipp_image_base.h'%XMIPP_BUNDLE), 'WRITE_'],
    'CastWriteMode': [fpath('%s/xmippCore/core/xmipp_image_base.h'%XMIPP_BUNDLE), 'CW_'],
    'MDLabel': [fpath('%s/xmippCore/core/metadata_label.h'%XMIPP_BUNDLE), ['MDL_', 'RLN_', 'BSOFT']],
    'XmippError': [fpath('%s/xmippCore/core/xmipp_error.h'%XMIPP_BUNDLE), 'ERR_']}


def WriteJavaEnum(class_name, header_file, pattern, log):
    java_file = fpath('java/src/xmipp/jni/%s.java' % class_name)
    env.Depends(java_file, header_file)
    fOut = open(java_file, 'w+')
    counter = 0;
    if isinstance(pattern, basestring):
        patternList = [pattern]
    elif isinstance(pattern, list):
        patternList = pattern
    else:
        raise Exception("Invalid input pattern type: %s" % type(pattern))
    last_label_pattern = patternList[0] + "LAST_LABEL"
    fOut.write("""package xmipp.jni;

public class %s {
""" % class_name)

    for line in open(header_file):
        l = line.strip();
        for p in patternList:
            if not l.startswith(p):
                continue
            if '///' in l:
                l, comment = l.split('///')
            else:
                comment = ''
            if l.startswith(last_label_pattern):
                l = l.replace(last_label_pattern, last_label_pattern + " = " + str(counter) + ";")
            if (l.find("=") == -1):
                l = l.replace(",", " = %d;" % counter)
                counter = counter + 1;
            else:
                l = l.replace(",", ";")

            fOut.write("   public static final int %s /// %s\n" % (l, comment))
    fOut.write("}\n")
    fOut.close()
    # Write log file
    if log:
        log.write("Java file '%s' successful generated at %s\n" %
                  (java_file, datetime.now()))


def ExtractEnumFromHeader(env, target, source):
    log = open(str(target[0]), 'w+')
    for (class_name, list) in javaEnumDict.iteritems():
        WriteJavaEnum(class_name, list[0], list[1], log)

    log.close()
    return None


env['JAVADIR'] = 'java'
env['JAVA_BUILDPATH'] = 'java/build'
env['JAVA_LIBPATH'] = 'java/lib'
env['JAVA_SOURCEPATH'] = 'java/src'
env['ENV']['LANG'] = 'en_GB.UTF-8'
env['JARFLAGS'] = '-Mcf'    # Default "cf". "M" = Do not add a manifest file.
# Set -g debug options if debugging
if debug:
    env['JAVAC'] = 'javac -g'  # TODO: check how to add -g without changing JAVAC

javaBuild = Execute(Mkdir(epath('java/build')))

# Update enums in java files from C++ headers. If they don't exist, generate them.
log = open(fpath('java/build/javaLog'), 'w+')
for class_name, class_list in javaEnumDict.iteritems():
    WriteJavaEnum(class_name, class_list[0], class_list[1], log)

javaExtractCommand = env.Command(
    epath('bindings/java/src/xmipp/jni/enums.changelog'),
    [fpath('%s/xmippCore/core/xmipp_image_base.h'%XMIPP_BUNDLE),
     fpath('%s/xmippCore/core/metadata_label.h'%XMIPP_BUNDLE)],
    ExtractEnumFromHeader)

javaEnums = env.Alias('javaEnums', javaExtractCommand)

imagejUntar = env.Untar(
    fpath('external/imagej/ij.jar'), fpath('external/imagej.tgz'),
    cdir=dpath('external'))
#env.Depends(imagejUntar, javaEnums)

ijLink = env.SymLink(fpath('java/lib/ij.jar'), imagejUntar[0].abspath)
env.Depends(ijLink, imagejUntar)
env.Default(ijLink)

xmippJavaJNI = env.AddJavaLibrary(
    'XmippJNI', 'xmipp/jni',
    deps=[ijLink, javaEnums])

xmippJavaUtils = env.AddJavaLibrary(
    'XmippUtils', 'xmipp/utils',
    deps=[ijLink, xmippJavaJNI])

xmippIJ = env.AddJavaLibrary(
    'XmippIJ', 'xmipp/ij/commons',
    deps=[xmippJavaUtils])

xmippViewer = env.AddJavaLibrary(
    'XmippViewer', 'xmipp/viewer',
    deps=[xmippIJ])

xmippTest = env.AddJavaLibrary(
    'XmippTest', 'xmipp/test',
    deps=[xmippViewer])


# FIXME: the environment used for the rest of SCons is imposible to
# use to compile java code. Why?
# In the meanwhile we'll use an alternative environment.
env2 = Environment(ENV=os.environ)
env2.AppendUnique(JAVACLASSPATH='"%s/*"' % dpath('java/lib'))
javaExtraFileTypes = env2.Java(epath('java/build/HandleExtraFileTypes.class'),
                               fpath('java/src/HandleExtraFileTypes.java'))
env2.Depends(javaExtraFileTypes, epath('java/lib/XmippViewer.jar'))
env2.Default(javaExtraFileTypes)

# FIXME: For any yet unknown issue, java is being compiled putting in
# -d flag the class name, producing a folder with the same name as the
# class and putting the class file inside
fileTypesInstallation = env.Install(
    dpath('external/imagej/plugins/Input-Output/'),
    epath('java/build/HandleExtraFileTypes.class/HandleExtraFileTypes.class'))
#env.Depends(fileTypesInstallation, pluginLink)
env.Default(fileTypesInstallation)

# Java tests
AddOption('--run-java-tests', dest='run_java_tests', action='store_true',
          help='Run all Java tests (not only default ones)')

env.AddJavaTest('FilenameTest', 'XmippTest.jar')
env.AddJavaTest('ImageGenericTest', 'XmippTest.jar')
env.AddJavaTest('MetadataTest', 'XmippTest.jar')

env.Alias('xmipp-java', [xmippJavaJNI,
                         xmippJavaUtils,
                         xmippIJ,
                         xmippViewer,
                         xmippTest])


#  ***********************************************************************
#  *                      Xmipp Scripts                                  *
#  ***********************************************************************


def addBatch(batchName, script, scriptFolder='applications/scripts'):
    """ Add a link to xmipp/bin folder prepending xmipp_ prefix.
    The script should be located in from xmipp root,
    by default in 'applications/scripts/'
    """
    xmippBatchName = 'xmipp_%s' % batchName
    batchLink = env.SymLink(join(XMIPP_PATH, 'bin', xmippBatchName),
                            join(XMIPP_PATH, scriptFolder, script))
    env.Alias('xmipp-batchs', batchLink)

    return batchLink


# Batches (apps)

addBatch('metadata_plot', 'metadata_plot/batch_metadata_plot.py')
addBatch('showj', 'showj/batch_showj.py')

XmippAlias = env.Alias('xmipp', ['xmipp-libs',
                                 'xmipp-java'])


Return('XmippAlias')
