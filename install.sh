#!/bin/bash

BASEDIR=$(dirname $0)
# cd $BASEDIR

help_msg() {
	echo "-----"
	echo "Basic usage:"
	echo "  install.sh"
	echo "    [-i|--install <DESTDIR>]"
	echo "    [-s|--source <DESTDIR>]"
	echo "    [-r|--revisions]"
	echo "-----"
}

install() {
	local outdir="$1/"	
	cd $BASEDIR
	echo "-----"
	echo "Install to directory $outdir"
	if [[ -d .git ]]; then
		print_revisions
	fi

	echo "Copying install files ..."
	mkdir -p $outdir
	mkdir -p $outdir/resource

	# save revisions in output directory
	if [[ -d .git ]]; then
		print_revisions > $outdir/revisions.txt
	fi

	# NOTE: cp -ar isn't cross-platform compatible, replaced with -pPR
	cp -pPR ./lib $outdir
	cp -pPR ./dist $outdir
	cp -pPR ./resource/dictionaries $outdir/resource
	cp -pPR ./LETAdicts $outdir
	cp -pPR ./Gazetteer $outdir
	cp -pPR ./models $outdir

	# PATCH (TODO, FIXME - remove when fixed): copy updated morpho model from LVTagger
	cp -pP ../LVTagger/models/lv-morpho-model.ser.gz $outdir/models

	cp -pP ./pipe.sh $outdir
	chmod a+x $outdir/pipe.sh	# just in case
	cp -pP ./pipe.bat $outdir
	cp -pP ./coref.prop_template $outdir
	cp -pP ./coref.prop_template $outdir/coref.prop		# everything not configured should be off here (knb etc.)
	# better not to reveal our secrets... ;) we can copy manually our coref.prop if we need to (for private use)
	# cp -pP ./coref.prop $outdir 2>/dev/null #copy if exists
	cp -pP ./lv-ner-tagger.prop $outdir
	echo "Finished install"
}

print_revisions() {
	local coref_rev=`git rev-parse HEAD`
	echo "Coref revision: ${coref_rev}" # in ${PWD}";
	(
		cd "./../LVTagger" &&
		local lvtagger_rev=`git rev-parse HEAD` &&
		echo "LVTagger revision: ${lvtagger_rev}" # in ${PWD}"
	)
	(
		cd "./../morphology";
		local morphology_rev=`git rev-parse HEAD`;
		echo "Morphology revision: ${morphology_rev}" # in ${PWD}";
	)
}

source_release() {
	local outdir="$1/"
	cd $BASEDIR
	echo "-----"
	echo "Make Coref source release to $outdir"
	print_revisions

	# save revisions in output directory
	mkdir -p $outdir
	print_revisions > $outdir/revisions.txt

	copy_repository $outdir/Coref	
	( cd ./../LVTagger; echo ${PWD}; copy_repository $outdir/LVTagger )
	( cd ./../morphology; copy_repository $outdir/morphology)

	# PATCH (TODO, FIXME - remove when fixed): copy updated morpho model from LVTagger
	cp -pP ../LVTagger/models/lv-morpho-model.ser.gz $outdir/Coref/models

	echo "Finished source release"
}

copy_repository() {
	echo "Copying ${PWD} git repository current files ..."
	local outdir=$1
	mkdir -p $outdir
	git ls-tree --name-only -r master | while read filename;
		do
			mkdir -p $outdir/`dirname "$filename"`
			cp "$filename" $outdir/"$filename"
		done
}

get_abs_filename() {
  # $1 : relative filename
  echo "$(cd "$(dirname "$1")" && pwd)/$(basename "$1")"
}


echo "======"
echo $(date)
echo "install.sh ${*}"

while [[ $# > 0 ]]
do
key="$1"
case $key in
    -i|--install)
	install $(get_abs_filename $2)
    shift
    ;;
    -s|--source)
    source_release $(get_abs_filename $2)
    shift
    ;;
	-r|--revisions)
    print_revisions
    ;;
	-h|-?|--help)
    help_msg
	exit
    shift
    ;;
    *)
    echo "Unknown option $key"
    ;;
esac
shift
done
