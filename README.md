# digred
DIGRaph EDitor

Copyright © 2020–2021, by Christopher Alan Mosher, Shelton, Connecticut, USA, cmosher01@gmail.com

[![License](https://img.shields.io/github/license/cmosher01/digred.svg)](https://www.gnu.org/licenses/gpl.html)
[![Website](https://img.shields.io/website/https/cmosher01.github.io/digred.svg)](https://cmosher01.github.io/digred)
[![Latest Release](https://img.shields.io/github/release-pre/cmosher01/digred.svg)](https://github.com/cmosher01/digred/releases/latest)
[![Build status](https://ci.appveyor.com/api/projects/status/sxag4ytyr54x0eoy?svg=true)](https://ci.appveyor.com/project/cmosher01/digred)
[![Donate](https://img.shields.io/badge/Donate-PayPal-green.svg)](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=CVSSQ2BWDCKQ2)

Defines a standard schema definition language for digraph databases (such as Neo4j).

```
Movie
    title : STRING

Actor
    name : STRING

Actor PLAYED_ROLE_IN Movie
    character : STRING
```

Also a cross-platform desktop application for user CRUD operations using a schema,
against a Neo4j database.
