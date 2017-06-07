# Bin scripts
These scripts are to generate meta files about the
translator, such as intermediary representations of
ASTs, header, and implementation files.

## Requirements
These scriptss requires GNU's `sed` tool.

### OSX installation
```sh
brew install gnu-sed
echo "PATH=\"/usr/local/opt/gnu-sed/libexec/gnubin:$PATH\"" >> ~/.bashrc
echo "MANPATH=\"/usr/local/opt/gnu-sed/libexec/gnuman:$MANPATH\"" >> ~/.bashrc
source ~/.bashrc
```

## Setup
### Linking the translator
```sh
# Run from the root of the source code directory
ln -s $(pwd)/bin/translateAll.sh $(pwd)/translateAll.sh
```
