import os
import platform
from xmipp_base import XmippScript, xmippExists, getXmippPath

class ScriptIJBase(XmippScript):
    def __init__(self, name):
        XmippScript.__init__(self)
        self.name = name
    
    def defineOtherParams(self):
        pass
    
    def readOtherParams(self):
        pass
    
    def defineParams(self):
        self.addParamsLine('  --input <...>            : Input files to show');
        self.addParamsLine('         alias -i;');
        self.addParamsLine('  [--memory <mem="2g">]    : Memory amount for JVM');
        self.addParamsLine('         alias -m;');
        self.defineOtherParams()
    
    def readInputFiles(self):
        self.inputFiles = self.getListParam('-i')
        
    def readParams(self):
        self.readInputFiles()
        self.memory = self.getParam('--memory')
        files = []
        missingFiles = []
        for f in self.inputFiles:
            if xmippExists(f):
                files.append('"%s"' % f) # Escape with " for filenames containing spaces
            else:
                missingFiles.append(f)
        self.inputFiles = files
        if len(missingFiles):
            print ("Missing files: \n %s" % '  \n'.join(missingFiles))
        
        self.args = "-i %s" % ' '.join(self.inputFiles)
        self.readOtherParams()
 
 
class ScriptPluginIJ(ScriptIJBase):
    def __init__(self, macro):
        ScriptIJBase.__init__(self, macro)
                  
    def run(self):
        runImageJPlugin(self.memory, self.name, self.args)
     
     
class ScriptAppIJ(ScriptIJBase):
    def __init__(self, name):
        ScriptIJBase.__init__(self, name)
                  
    def run(self):
        if len(self.inputFiles) > 0:
            runJavaIJapp(self.memory, self.name, self.args, batchMode=False)
        else:
            print ("No input files. Exiting...")
            
    
def getImageJPluginCmd(memory, macro, args, batchMode=False):
    if len(memory) == 0:
        memory = "1g"
        print ("No memory size provided. Using default: " + memory)
    imagej_home = getXmippPath('external', 'imagej')
    plugins_dir = os.path.join(imagej_home, "plugins")
    macro = os.path.join(imagej_home, "macros", macro)
    imagej_jar = os.path.join(imagej_home, "ij.jar")
    cmd = """ java -Xmx%s -Dplugins.dir=%s -jar %s -macro %s "%s" """ % (memory, plugins_dir, imagej_jar, macro, args)
    if batchMode:
        cmd += " &"
    return cmd


def runImageJPlugin(memory, macro, args, batchMode=False):
    os.system(getImageJPluginCmd(memory, macro, args, batchMode))

def getJavaIJappCmd(memory, appName, args, batchMode=False):
    '''Launch an Java application based on ImageJ '''
    if len(memory) == 0:
        memory = "2g"
        print ("No memory size provided. Using default: " + memory)
    imagej_home = getXmippPath("bindings", "java", "imagej")
    lib = getXmippPath("lib")
    javaLib = getXmippPath('bindings', 'java', 'lib')
    plugins_dir = os.path.join(imagej_home, "plugins")
    cmd = "java -Xmx%(memory)s -Djava.library.path=%(lib)s -Dplugins.dir=%(plugins_dir)s -cp %(imagej_home)s/*:%(javaLib)s/* %(appName)s %(args)s" % locals()
    if batchMode:
        cmd += " &"
    return cmd
    
def runJavaIJapp(memory, appName, args, batchMode=True):
    cmd = getJavaIJappCmd(memory, appName, args, batchMode)
    print (cmd)
    os.system(cmd)
    
def runJavaJar(memory, jarName, args, batchMode=True):
    jarPath = getXmippPath(jarName)
    runJavaIJapp(memory, '-jar %s' % jarPath, args, batchMode)



class ScriptShowJ(ScriptAppIJ):
    def __init__(self, viewer='xmipp.viewer.Viewer'):
        ScriptAppIJ.__init__(self, viewer)
        
    def defineOtherParams(self):
        self.addParamsLine('  [--mode <mode_value=image>]           : List of params ')
        self.addParamsLine('     where <mode_value> image gallery metadata rotspectra')
        self.addParamsLine('         alias -d;')
        self.addParamsLine('  [--poll]                            : Keeps checking for changes on input files  (for image mode only!)')
        self.addParamsLine('         alias -p;')
        self.addParamsLine('  [--render <...>]    : Specifies image columns to render (for metadata mode only)')
        self.addParamsLine('                          : by default the first one that can be visualized is rendered')
        self.addParamsLine('  [--visible <...>]    : Specifies visible labels')
        self.addParamsLine('  [--order <...>]    : Specifies labels order')
        self.addParamsLine('  [--labels <...>]    : Specifies labels to display')
        self.addParamsLine('  [--sortby <...>]    : Specifies label to sort by. asc or desc mode can be added')
        
        self.addParamsLine('         alias -e;')
        self.addParamsLine('  [--rows <rows>]                            : number of rows in table')
        self.addParamsLine('         alias -r;')
        self.addParamsLine('  [--columns <columns>]                            : number of columns in table')
        self.addParamsLine('         alias -c;')
        self.addParamsLine('  [--zoom <zoom>]                            : zoom for images')
        self.addParamsLine('         alias -z;')
        self.addParamsLine('  [--view <axis="z">]                        : Viewer position (for volumes only)')
        self.addParamsLine('     where <axis> z y x z_pos y_pos x_pos')
        self.addParamsLine('  [--dont_apply_geo]                        : Does not read geometrical information(for metadata only)')
        self.addParamsLine('  [--dont_wrap]                             : Does not wrap (for metadata only)')
        self.addParamsLine('  [--debug] : debug')
        self.addParamsLine('  [--mask_toolbar] : Open mask toolbar (only valid for images)')
        self.addParamsLine('  [--label_alias <alias_string>]  : Activate some metadata label alias, for example')
        self.addParamsLine('                                  : anglePsi=aPsi;shiftX=sX;shiftY:sY')
        self.addParamsLine('  [--label_relion]                : Activates the mapping to Relion labels')
        self.addParamsLine('  [--label_bsoft]                 : Activates the mapping to Bsoft labels')
        
    def readOtherParams(self):
        #FIXME: params seems to be they cannot be passed directly to java
        params = ['--mode', '--rows', '--columns', '--zoom', '--view', '--sortby']
        for p in params:
            if self.checkParam(p):
                self.args += " %s %s" % (p, self.getParam(p))
        params = [ '--render', '--visible', '--order', '--labels']
        pvalues = ''
        for p in params:
            if self.checkParam(p):
                for pvalue in self.getListParam(p):
                    pvalues = '%s %s'%(pvalues, pvalue)
                    
                self.args += " %s %s" % (p, pvalues)
        params = ['--poll', '--debug', '--dont_apply_geo', '--dont_wrap', '--mask_toolbar']
        for p in params:
            if self.checkParam(p):
                self.args += " %s" % p
                
        # Set environment var for extra label alias
        if self.checkParam('--label_alias'):
            os.environ['XMIPP_EXTRA_ALIASES'] = self.getParam('--label_alias')
        elif self.checkParam('--label_bsoft'):
            from protlib_import import bsoftLabelString
            os.environ['XMIPP_EXTRA_ALIASES'] = bsoftLabelString()
        elif self.checkParam('--label_relion') or self.getParam('-i').endswith('.star'):
            from protlib_import import relionLabelString
            os.environ['XMIPP_EXTRA_ALIASES'] = relionLabelString()
            
