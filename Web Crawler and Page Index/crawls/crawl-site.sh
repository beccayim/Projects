#!/bin/bash

# SITE='norvig.com'
# DOMAIN='norvig.com'

# SITE='cs.gmu.edu/~white'
# DOMAIN='cs.gmu.edu'

# SITE='cs.gmu.edu/~sean/'
# DOMAIN='cs.gmu.edu'

# SITE='www.cs.utexas.edu/~EWD'
# DOMAIN='www.cs.utexas.edu'

SITE='kias.dyndns.org/'
DOMAIN='kias.dyndns.org'

wget \
     --recursive \
     --page-requisites \
     --adjust-extension \
     --convert-links \
     --restrict-file-names=windows \
     --domains "$DOMAIN" \
     --reject pdf,zip,ps,bib,tar.gz,pkg,sit \
     --no-parent \
     "$SITE"

     # --accept html,HTML,jpg,jpeg,JPG,JPEG,png,PNG,gif,GIF,css,js,research \

     # --accept html,HTML \
     # --no-clobber \
