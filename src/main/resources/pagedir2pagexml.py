#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Mon Dec 18 10:40:26 2017
@author: anb51nh
"""

from os import path
from glob import glob
from time import gmtime, strftime
from math import sin, cos, radians
import argparse
from lxml import etree
from PIL import Image



def pagexmlcombine(ocrindex, gtindex, xmlfile, output):

    xmlfile = path.abspath(xmlfile)
    pagedir = path.split(xmlfile)[0] + '/Pages'    
    commentsdir = path.split(xmlfile)[0] + '/Comments'
    pagename = path.splitext(path.basename(xmlfile))[0]
    
    thispagedir = pagedir + '/' + pagename
    commentsfile = commentsdir + '/' + pagename + '.txt'
    
    # load xml
    root = etree.parse(xmlfile).getroot()
    ns = {"ns":root.nsmap[None]}
    
    #convert point notation (older PageXML versions)
    for c in root.xpath("//ns:Coords[not(@points)]", namespaces=ns):
        cc = []
        for point in c.xpath("./ns:Point", namespaces=ns):
            cx = point.attrib["x"]
            cy = point.attrib["y"]
            c.remove(point)
            cc.append(cx+","+cy)
        c.attrib["points"] = " ".join(cc)    
    
    # combine data in coordmap dict
    textregions = root.xpath('//ns:TextRegion', namespaces=ns)
    coordmap = {}
    for r in textregions:
        rid = r.attrib["id"]
        coordmap[rid] = {"type":r.attrib["type"]}
        
        # coordinates
        coordmap[rid]["coords"] = []
        for c in r.xpath("./ns:Coords", namespaces=ns) + r.xpath("./Coords"):
            coordstrings = [x.split(",") for x in c.attrib["points"].split()]
            coordmap[rid]["coords"] += [(int(x[0]), int(x[1])) for x in coordstrings ]
            
        # find region dir, offset and size
        for imgf in glob(thispagedir + "/*" + coordmap[rid]["type"] + ".png"):
            if not "offset" in coordmap[rid]:
                size = Image.open(imgf).size
                offsetp = path.splitext(imgf)[0] + ".offset"
                with open(offsetp) as f:
                    offset = tuple([int(x) for x in f.read().split(",")])
                # check if coordinates fit in region rectangle
                fit = all([offset[0]<=c[0]<=offset[0]+size[0] \
                         and  offset[1]<=c[1]<=offset[1]+size[1] \
                    for c in coordmap[rid]["coords"]])
                if fit:
                    coordmap[rid]["offset"] = offset
                    coordmap[rid]["size"] = size
                    coordmap[rid]["path"] = path.splitext(imgf)[0]
        if not "path" in coordmap[rid]:
            raise ValueError("Segment for region " + rid + " not found in pagedir "+thispagedir+"!")
            
        # angle
        if path.isfile(coordmap[rid]["path"] + ".angle"):
            with open(coordmap[rid]["path"] + ".angle") as f:
                coordmap[rid]["angle"] = float(f.read())
        else:
            coordmap[rid]["angle"] = 0.0
            
        
        # lines
        coordmap[rid]["lines"] = {}
        for n, l in enumerate(sorted(glob(coordmap[rid]["path"]+"/*.coords"))):
            lpath = path.splitext(l)[0]
            lid = '{}_{:03d}'.format(rid, n + 1)
            coordmap[rid]["lines"][lid] = {}
            with open(l) as f:
                b = f.read().split(",")
            b = [int(x) for x in b] 
            b = [b[1], b[0], b[3], b[2]] # boxes: [(x1, y1, x2, y2),...], cf. kraken segmenter
            coords = [(b[0],b[1]),(b[2],b[1]),(b[2],b[3]),(b[0],b[3])]
            
            # rotate line coordinates 
            if coordmap[rid]["angle"]:
                newcoords = []
                center = tuple([x/2 for x in coordmap[rid]["size"]])
                a = radians(- coordmap[rid]["angle"])
                for c in coords:
                    x = c[0] - center[0]
                    y = c[1] - center[1]
                    x =  x*cos(a) + y*sin(a)
                    y = -x*sin(a) + y*cos(a)
                    x = round(x + center[0])
                    y = round(y + center[1])
                    newcoords.append((x,y))
                coords = newcoords
            
            # relative to absolute coordinates
            coords = [(x[0]+coordmap[rid]["offset"][0], x[1]+coordmap[rid]["offset"][1]) for x in coords]
            coordmap[rid]["lines"][lid]["coords"] = coords
            
            # ocr text
            for fpath in glob(lpath + ".pred.txt"):
                with open(fpath, encoding='utf-8') as f:
                    coordmap[rid]["lines"][lid]["ocr"] =  f.read().strip()
                
            # gt text
            for fpath in glob(lpath + ".gt.txt"):
                with open(fpath, encoding='utf-8') as f:
                    coordmap[rid]["lines"][lid]["gt"] =  f.read().strip()
                    
    # start writing coordmap back to xml
    for rid in sorted(coordmap):
        textregion = root.find('.//ns:TextRegion[@id="'+rid+'"]', namespaces=ns)
        regiontext = []

        # angle
        if coordmap[rid]["angle"]:
            textregion.attrib["orientation"] = str(-1 * coordmap[rid]["angle"])
        # lines
        for lid in coordmap[rid]["lines"]:
            linexml = textregion.find('./ns:TextLine[@id="'+lid+'"]', namespaces=ns)
            if linexml is None:
                linexml = etree.SubElement(textregion, "TextLine", attrib={"id":lid})
            # coords
            coordsxml = linexml.find('./ns:Coords', namespaces=ns)
            if coordsxml is None:
                coordsxml = etree.SubElement(linexml, "Coords")
            coordsxml.attrib["points"] = " ".join(str(x[0])+","+str(x[1]) \
                            for x in coordmap[rid]["lines"][lid]["coords"])
            
            # text
            if "ocr" in coordmap[rid]["lines"][lid]:
                textequivxml = linexml.find('./ns:TextEquiv[@index="'+str(ocrindex)+'"]', namespaces=ns)
                if textequivxml is None:
                    textequivxml = etree.SubElement(linexml, "TextEquiv", attrib={"index":str(ocrindex)})
                unicodexml = textequivxml.find('./ns:Unicode', namespaces=ns)
                if unicodexml is None:
                    unicodexml = etree.SubElement(textequivxml, "Unicode")
                unicodexml.text = coordmap[rid]["lines"][lid]["ocr"]
            if "gt" in coordmap[rid]["lines"][lid]:
                textequivxml = linexml.find('./ns:TextEquiv[@index="'+str(gtindex)+'"]', namespaces=ns)
                if textequivxml is None:
                    textequivxml = etree.SubElement(linexml, "TextEquiv", attrib={"index":str(gtindex)})
                unicodexml = textequivxml.find('./ns:Unicode', namespaces=ns)
                if unicodexml is None:
                    unicodexml = etree.SubElement(textequivxml, "Unicode")
                unicodexml.text = coordmap[rid]["lines"][lid]["gt"]
            
        # region text collect
        for lid in coordmap[rid]["lines"]:
            if "gt" in coordmap[rid]["lines"][lid]:
                regiontext.append(coordmap[rid]["lines"][lid]["gt"])
            elif "ocr" in coordmap[rid]["lines"][lid]:
                regiontext.append(coordmap[rid]["lines"][lid]["ocr"])
            else:
                regiontext.append("")
                    
        # region text insert
        textequivxml = textregion.find('./ns:TextEquiv', namespaces=ns)
        if textequivxml is None:
            textequivxml = etree.SubElement(textregion, "TextEquiv")
        unicodexml = textequivxml.find('./ns:Unicode', namespaces=ns)
        if unicodexml is None:
            unicodexml = etree.SubElement(textequivxml, "Unicode")
        unicodexml.text = "\n".join(regiontext)
    
    # timestamp
    lastchange = root.find('.//ns:LastChange', namespaces=ns)
    lastchange.text = strftime("%Y-%m-%dT%H:%M:%S", gmtime())
    
    # comments
    if path.isfile(commentsfile):
        metadata = root.find('.//ns:Metadata', namespaces=ns)
        commentsxml = metadata.find('./ns:Comments', namespaces=ns)
        if commentsxml is None:
            commentsxml = etree.SubElement(metadata, "Comments")
        with open(commentsfile) as f:
            commentsxml.text = f.read()
    
    # update version
    xmlcontent = etree.tounicode(root.getroottree()).replace(
             "http://schema.primaresearch.org/PAGE/gts/pagecontent/2010-03-19",
             "http://schema.primaresearch.org/PAGE/gts/pagecontent/2017-07-15"
            ).replace(
             "http://schema.primaresearch.org/PAGE/gts/pagecontent/2013-07-15",
             "http://schema.primaresearch.org/PAGE/gts/pagecontent/2017-07-15"                    
            )
    xmlcontent = '<?xml version="1.0" encoding="UTF-8" standalone="no"?>' + xmlcontent
    
    # write file
    with open(path.abspath(output), "w", encoding='utf-8') as f:
        f.write(xmlcontent)
        
        
        
def loopfiles(ocrindex, gtindex, xmlfiles, output):
    """
        Takes a bunch of PageXML files and integrates information retrieved from
        a subfolder called 'Pages' in the same directory.
    """
    if xmlfiles:
        for xmlfile in xmlfiles:
            pagexmlcombine(ocrindex, gtindex, xmlfile, output)


parser = argparse.ArgumentParser("""
XML output generation tool
""")
parser.add_argument('-ocrx','--ocrindex',type=int,default=1,help='Index attribute of the OCR text.')
parser.add_argument('-gtx','--gtindex',type=int,default=0,help='Index attribute of the ground truth text.')
parser.add_argument('-o','--output', type=str, help='Output directory')
parser.add_argument('xmlfiles',nargs='+')
args = parser.parse_args()

if __name__ == '__main__':
    loopfiles(args.ocrindex, args.gtindex, args.xmlfiles, args.output)
