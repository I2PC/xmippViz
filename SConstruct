#!/usr/bin/env python3

# **************************************************************************
# *
# * Authors:     I. Foche Perez (ifoche@cnb.csic.es)
# *              J. Burguet Castell (jburguet@cnb.csic.es)
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

# Builders and pseudobuilders used be SConscript to install things.

EnsurePythonVersion(3, 3)

import os
import sys
import shutil
from os.path import join

try:
    from itertools import izip
except:
    izip = zip
from glob import glob
import fnmatch
import platform
from SCons import SConf, Node, Script

from configparser import ConfigParser, ParsingError

MACOSX = (platform.system() == 'Darwin')
WINDOWS = (platform.system() == 'Windows')
LINUX = (platform.system() == 'Linux')

# Define our builders
if LINUX:
    download = Builder(action='wget -nv -c -O $TARGET $SOURCE')
    untar = Builder(action='tar -C $cdir --recursive-unlink -xzf $SOURCE')
elif MACOSX:
    download = Builder(action='curl -L "$SOURCE" -o "$TARGET"')
    untar = Builder(action='tar -C $cdir -xzf $SOURCE')
else:
    print('OS not tested yet')
    sys.exit(1)

# Create the environment the whole build will use.
env = Environment(ENV=os.environ,
                  BUILDERS=Environment()['BUILDERS'],
                  tools=['Make', 'AutoConfig'],
                  toolpath=[join('install', 'scons-tools')])
# TODO: BUILDERS var added from the tricky creation of a new environment.
# If not, they lose default builders like "Program", which are needed later
# (by CheckLib and so on). See http://www.scons.org/doc/2.0.1/HTML/scons-user/x3516.html
# See how to change it into a cleaner way (not doing BUILDERS=Environment()['BUILDERS']!)

AddOption('--verbose', dest='verbose', action='store_true',
          help='Show full message of compilation lines')
# Message from autoconf and make, so we don't see all its verbosity.
if not GetOption('verbose'):
    env['AUTOCONFIGCOMSTR'] = "Configuring $TARGET from $SOURCES"
    env['MAKECOMSTR'] = "Compiling & installing $TARGET from $SOURCES "


def targetInBuild(env, targetName):
    return targetName in map(str, BUILD_TARGETS)


# Add the path to dynamic libraries so the linker can find them.

if LINUX:
    env.AppendUnique(LIBPATH=os.environ.get('LD_LIBRARY_PATH', ''))
elif MACOSX:
    env.AppendUnique(LIBPATH=os.environ.get('DYLD_FALLBACK_LIBRARY_PATH', ''))
elif WINDOWS:
    print("OS not tested yet")
    sys.exit(1)
else:
    print("Unknown system: %s\nPlease tell the developers." % platform.system())


# Python and SCons versions are fixed
# env.EnsurePythonVersion(2,7)
# env.EnsureSConsVersion(2,3,2)
# TODO: see after all is clean and crispy if we can avoid fixing the versions.
# We can specify a range of valid version after we check it works with them.


#  ************************************************************************
#  *                                                                      *
#  *                       Auxiliar functions                             *
#  *                                                                      *
#  ************************************************************************


def appendUnique(elist, element):
    """Add element to a list only if it doesnt previously exist"""
    if element not in elist:
        if not isinstance(element, str):
            elist.extend(element)
        else:
            elist.append(element)


#  ************************************************************************
#  *                                                                      *
#  *                            Extra options                             *
#  *                                                                      *
#  ************************************************************************
cf = ConfigParser()
cf.optionxform = str  # keep case (stackoverflow.com/questions/1611799)
try:
    cf.read_file(open("../../xmipp.conf"))
except OSError:
    sys.exit("Config file not found.")
except ParsingError:
    sys.exit("%s\nError while parsing the config file." % sys.exc_info()[1])
if not 'BUILD' in cf.sections():
    print("Cannot find section BUILD in the config file.")
os.environ.update(dict(cf.items('BUILD')))

env['CPPPATH'] = os.environ.get('CPPPATH', [])
env['CC'] = os.environ.get('CC')
env['CXX'] = os.environ.get('CXX')
env['LINKERFORPROGRAMS'] = os.environ.get('LINKERFORPROGRAMS')
env['CCFLAGS'] = os.environ.get('CCFLAGS', '').split()
cxxFlags = os.environ.get('CXXFLAGS', '')
if os.environ.get('DEBUG', '0') == 'True':  # FIXME, use 1, true, yes...
    cxxFlags += ' -g'
else:
    if cxxFlags.find("-O") == -1:
        cxxFlags += (
            " -O3" if 'TRAVIS' not in os.environ else " -O0")  # don't optimize on Travis, as it slows down the build
env['CXXFLAGS'] = cxxFlags.split()
os.environ['CXXFLAGS'] = cxxFlags  # FIXME use only env or os.environ in the rest of the code
env['LINKFLAGS'] = os.environ.get('LINKFLAGS', '').split()

for path in ['JAVA_HOME', 'JAVA_BINDIR']:
    if not os.path.isdir(os.environ.get(path, '')):
        Exit('Path to $%s (%s) should exist, but it does not. Stopping.\n'
             'Please run "scipion config"' % (path, os.environ.get(path, '')))

# Java related environment variables, probably the main one
# that need to be modified is JAVA_HOME
env['JAVA_HOME'] = os.environ['JAVA_HOME']
env['JAVA_BINDIR'] = os.environ['JAVA_BINDIR']
env['JAVAC'] = os.environ.get('JAVAC')
env['JAR'] = os.environ.get('JAR')
env['JNI_CPPPATH'] = os.environ.get('JNI_CPPPATH').split(':')

xmippPath = Dir('.').abspath
env['PACKAGE'] = {'NAME': 'xmipp',
                  'SCONSCRIPT': xmippPath
                  }


#  ************************************************************************
#  *                                                                      *
#  *                           Pseudobuilders                             *
#  *                                                                      *
#  ************************************************************************

def remove_prefix(text, prefix):
    return text[text.startswith(prefix) and len(prefix):]


env['INCDIRFLAGS'] = os.environ.get('INCDIRFLAGS', '').split()
env['LIBDIRFLAGS'] = os.environ.get('LIBDIRFLAGS', '').split()

if len(env["INCDIRFLAGS"]) > 0:
    external_incdirs = [remove_prefix(os.path.expandvars(x), "-I") for x in env["INCDIRFLAGS"]]
else:
    external_incdirs = []

if len(env["LIBDIRFLAGS"]) > 0:
    external_libdirs = [remove_prefix(os.path.expandvars(x), "-L") for x in env["LIBDIRFLAGS"]]
else:
    external_libdirs = []

env['EXTERNAL_INCDIRS'] = external_incdirs
env['EXTERNAL_LIBDIRS'] = external_libdirs


def addCppLibrary(env, name, dirs=[], tars=[], untarTargets=['configure'], patterns=[], incs=[],
                  libs=[], prefix=None, suffix=None, installDir=None, libpath=['lib'], deps=[],
                  mpi=False, cuda=False, default=True, target=None):
    """Add self-made and compiled shared library to the compilation process
    
    This pseudobuilder access given directory, compiles it
    and installs it. It also tells SCons about it dependencies.

    If default=False, the library will not be built unless the option
    --with-<name> is used.

    Returns the final targets, the ones that Make will create.
    """
    _libs = list(libs)
    _libpath = list(libpath) + external_libdirs
    if name == "XmippReconsAdaptCuda":
        _libs.append("XmippReconsCuda")
        _libs.append("cudart")
        _libs.append("cuda")
        _libs.append("cufft")
        _libpath.append(env['CUDA_LIB'])
    if name == "XmippParallel" and cuda == True:
        _libs.append("XmippReconsAdaptCuda")
    _incs = list(incs) + external_incdirs
    lastTarget = deps
    prefix = 'lib' if prefix is None else prefix
    suffix = '.so' if suffix is None else suffix

    basedir = 'lib'
    targetName = join(basedir, target if target else prefix + name)
    sources = []

    for d, p in izip(dirs, patterns):
        sources += glob(join(env['PACKAGE']['SCONSCRIPT'], d, p))

    if not sources and env.TargetInBuild(name):
        Exit('No sources found for Library: %s. Exiting!!!' % name)

    env2 = Environment()
    env2['ENV']['PATH'] = env['ENV']['PATH']

    mpiArgs = {}
    if mpi:
        _libpath.append(env['MPI_LIBDIR'])
        _libs.append(env['MPI_LIB'])
        _incs.append(env['MPI_INCLUDE'])

        mpiArgs = {'CC': env['MPI_CC'],
                   'CXX': env['MPI_CXX'],
                   'LINK': env['MPI_LINKERFORPROGRAMS']}
        #         conf = Configure(env, custom_tests = {'CheckMPI': CheckMPI})
        #         if not conf.CheckMPI(env['MPI_INCLUDE'], env['MPI_LIBDIR'],
        #                              env['MPI_LIB'], env['MPI_CC'], env['MPI_CXX'],
        #                              env['MPI_LINKERFORPROGRAMS'], False):
        #             print >> sys.stderr, 'ERROR: MPI is not properly working. Exiting...'
        #             Exit(1)
        #         env = conf.Finish()
        env2.PrependENVPath('PATH', env['MPI_BINDIR'])

    _incs.append(env['CPPPATH'])

    library = env2.SharedLibrary(
        target=targetName,
        # source=lastTarget,
        source=sources,
        CPPPATH=_incs,
        LIBPATH=_libpath,
        LIBS=_libs,
        SHLIBPREFIX=prefix,
        SHLIBSUFFIX=suffix,
        CXXFLAGS=env['CXXFLAGS'] + env['INCDIRFLAGS'],
        LINKFLAGS=env['LINKFLAGS'] + env['LIBDIRFLAGS'],
        **mpiArgs
    )
    SideEffect('dummy', library)
    env.Depends(library, sources)

    if installDir:
        install = env.Install(installDir, library)
        SideEffect('dummy', install)
        lastTarget = install
    else:
        lastTarget = library
    env.Default(lastTarget)

    for dep in deps:
        env.Depends(sources, dep)

    env.Alias(name, lastTarget)

    return lastTarget


def symLink(env, target, source):
    # As the link will be in bin/ directory we need to move up
    sources = source
    current = Dir('.').path + '/'
    if isinstance(target, Node.NodeList) or isinstance(target, list):
        link = target[0].path
    else:
        link = target
    if isinstance(link, str) and link.startswith(current):
        link = link.split(current)[1]
    if isinstance(sources, Node.NodeList) or isinstance(sources, list):
        sources = source[0].path
    if isinstance(sources, str) and sources.startswith(current):
        sources = sources.split(current)[1]

    sources = os.path.relpath(sources, os.path.split(link)[0])
    # if os.path.lexists(link):
    #    os.remove(link)
    # print 'Linking to %s from %s' % (sources, link)
    # os.symlink(sources, link)
    result = env.Command(Entry(link),
                         Entry(source),
                         Action('rm -rf %s && ln -v -s %s %s' % (Entry(link).abspath, sources,
                                                                 Entry(link).abspath),
                                'Creating a link from %s to %s' % (link, sources)))
    env.Default(result)
    return result


def Cmd(cmd):
    print(cmd)
    os.system(cmd)


def AddMatchingFiles(params, directory, files):
    """ Callback, adds all matching files in dir
        params[0] = pattern
        params[1] = blacklist
        params[2] = sources
    """
    for filename in fnmatch.filter(files, params[0]):
        if filename not in params[1]:
            params[2].append(join(directory, filename))


def Glob(path, pattern, blacklist=[]):
    """ Custom made globbing, walking into all subdirectories from path. """
    sources = []
    for root, dirs, files in os.walk(path):
        for file in fnmatch.filter(files, pattern):
            if file not in blacklist:
                sources.append(join(root, file))
    return sources


def CreateFileList(path, pattern, filename, root='', root2=''):
    fOut = open(filename, 'w+')
    files = [f.replace(root, root2) + '\n' for f in Glob(path, pattern, [])]
    fOut.writelines(files)
    fOut.close()


def CompileJavaJar(target, source, env):
    """Add self-made and compiled java library to the compilation process """
    srcDir = str(source[0])
    print("Compiling jar: ", target[0])
    buildDir = join(env['PACKAGE']['SCONSCRIPT'], env['JAVA_BUILDPATH'])
    classPath = "'%s/*'" % join(env['PACKAGE']['SCONSCRIPT'], env['JAVA_LIBPATH'])
    globalSrcDir = join(env['PACKAGE']['SCONSCRIPT'], env['JAVA_SOURCEPATH'])
    jarfile = str(target[0])
    name = os.path.basename(jarfile)
    listfile = join(buildDir, name + '_source.txt')
    classfile = join(buildDir, name + '_classes.txt')
    CreateFileList(srcDir, '*.java', listfile)
    Cmd(env['JAVAC'] + ' -cp %(classPath)s -d %(buildDir)s -sourcepath %(srcDir)s @%(listfile)s' % locals())

    classDir = join(buildDir, os.path.relpath(srcDir, globalSrcDir))
    # This is needed for compiling IJ plugins
    # where the file 'plugins.config' need to be include in the final .jar file
    configFile = 'plugins.config'
    pluginDest = ''
    if os.path.exists(join(srcDir, configFile)):
        pluginDest = join(classDir, configFile)
        Cmd('cp %s %s' % (join(srcDir, configFile), pluginDest))
    CreateFileList(classDir, '*.class', classfile, buildDir + '/', '-C %(buildDir)s ' % locals())
    jarFlags = env['JARFLAGS']
    Cmd(env['JAR'] + ' %(jarFlags)s %(jarfile)s @%(classfile)s %(pluginDest)s' % locals())


def addJavaLibrary(env, name, path, deps=[], default=True):
    """ Add self-made and compiled java library to the compilation process
    This pseudobuilder access given directory, compiles it
    and installs it. It also tells SCons about it dependencies.

    If default=False, the library will not be built unless the option
    --with-<name> is used.

    Returns the final targets, the ones that Make will create.
    """
    libPath = join(env['PACKAGE']['SCONSCRIPT'], env['JAVA_LIBPATH'])
    srcPath = join(env['PACKAGE']['SCONSCRIPT'], env['JAVA_SOURCEPATH'])

    # Get all java files inside the source 
    libSrcPath = join(srcPath, path)
    sources = Glob(libSrcPath, "*.java")

    jar = '%s.jar' % name
    jarfile = join(libPath, jar)
    jarCreation = env.Command(jarfile, [libSrcPath], CompileJavaJar)

    for sd in sources + deps:
        env.Depends(jarCreation, sd)

    env.Alias(jar, jarCreation)
    if default:
        env.Default(jar)

    packageName = env['PACKAGE']['NAME']
    env.Alias(packageName + '-java', jarCreation)

    return jarCreation


def addJavaTest(env, name, source, installDir=None, default=True):
    """Add java test to the compilation process
    
    This pseudobuilder executes a java test using the Command builder.

    If default=False, the test will not be done unless the option
    --with-java-tests is used.

    Returns the final targets, the ones that Command returns.
    """
    if not env.TargetInBuild('run_java_tests'):
        return ''
    installDir = installDir or 'java/lib'
    classPath = ":".join(glob(join(Dir(installDir).abspath, '*.jar')))
    cmd = '%s -cp %s org.junit.runner.JUnitCore xmipp.test.%s' % (join(env['JAVA_BINDIR'], 'java'), classPath, name)
    runTest = env.Command(name, join(installDir, source), cmd)
    env.Alias('run_java_tests', runTest)
    env.Default(runTest)

    return runTest


def compilerConfig(env):
    """Check the good state of the C and C++ compilers and return the proper env."""

    conf = Configure(env)
    # ---- check for environment variables
    if 'CC' in os.environ:
        conf.env.Replace(CC=os.environ['CC'])
    else:
        conf.env.Replace(CC='gcc')
    print(">> Using C compiler: " + conf.env.get('CC'))

    if 'CFLAGS' in os.environ:
        conf.env.Replace(CFLAGS=os.environ['CFLAGS'])
        print(">> Using custom C build flags")

    if 'CXX' in os.environ:
        conf.env.Replace(CXX=os.environ['CXX'])
    else:
        conf.env.Replace(CXX='g++')
    print(">> Using C++ compiler: " + conf.env.get('CXX'))

    if 'CXXFLAGS' in os.environ:
        conf.env.Append(CPPFLAGS=os.environ['CXXFLAGS'])
        print(">> Appending custom C++ build flags : " + os.environ['CXXFLAGS'])

    if 'LDFLAGS' in os.environ:
        conf.env.Append(LINKFLAGS=os.environ['LDFLAGS'])
        print(">> Appending custom link flags : " + os.environ['LDFLAGS'])

    conf.CheckCC()
    conf.CheckCXX()
    env = conf.Finish()
    return env


def libraryTest(env, name, lang='c'):
    """Check the existence of a concrete C/C++ library."""
    env2 = Environment(LIBS=env.get('LIBS', ''))
    conf = Configure(env2)
    conf.CheckLib(name, language=lang)
    env2 = conf.Finish()
    # conf.Finish() returns the environment it used, and we may want to use it,
    # like:  return conf.Finish()  but we don't do that so we keep our env clean :)


# Add methods so SConscript can call them.
env.AddMethod(untar, 'Untar')
env.AddMethod(compilerConfig, 'CompilerConfig')
env.AddMethod(addCppLibrary, 'AddCppLibrary')
env.AddMethod(addJavaLibrary, 'AddJavaLibrary')
env.AddMethod(symLink, 'SymLink')
env.AddMethod(addJavaTest, 'AddJavaTest')
env.AddMethod(targetInBuild, 'TargetInBuild')

# Run SConscript
env.SConscript('SConscript', exports='env')

# Add original help (the one that we would have if we didn't use
# Help() before). But remove the "usage:" part (first line).
phelp = Script.Main.OptionsParser.format_help().split('\n')
Help('\n'.join(phelp[1:]))
# This is kind of a hack, because the #@!^ scons doesn't give you easy
# access to the original help message.
