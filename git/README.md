# Git Scripts
## Description
Scipts that can run pre/post git commands.

## Setup
### Automatic formatting before every commit:
```sh
# Run from the root of the source code directory
ln -s $(pwd)/git/pre-commit.sh $(pwd)/.git/hooks/pre-commit
```

### Automatic formatting before every push + run all tests:
```sh
# Run from the root of the source code directory
ln -s $(pwd)/git/pre-push.sh $(pwd)/.git/hooks/pre-push
```
