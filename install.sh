#!/bin/sh

BASEDIR=$(dirname $0)
cd $BASEDIR

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
	echo "-----"
	echo "Install to directory $outdir"
	print_revisions

	echo "Copying install files ..."
	mkdir -p $outdir
	mkdir -p $outdir/resource

	cp -ar ./lib $outdir
	cp -ar ./dist $outdir
	cp -ar ./resource/dictionaries $outdir/resource
	cp -ar ./LETAdicts $outdir
	cp -ar ./Gazetteer $outdir
	cp -ar ./models $outdir

	cp -a ./pipe.sh $outdir
	cp -a ./pipe.bat $outdir
	cp -a ./coref.prop_template $outdir/coref.prop
	cp -a ./coref.prop $outdir 2>/dev/null #copy if exists
	cp -a ./lv-ner-tagger.prop $outdir
	echo "Finished install"
}

print_revisions() {
	local coref_rev=`git rev-parse HEAD`
	echo "Coref revision: ${coref_rev} in ${PWD}";
	(
		cd "./../LVTagger" &&
		local lvtagger_rev=`git rev-parse HEAD` &&
		echo "LVTagger revision: ${lvtagger_rev} in ${PWD}"
	)
	(
		cd "./../morphology";
		local morphology_rev=`git rev-parse HEAD`;
		echo "Morphology revision: ${morphology_rev} in ${PWD}";
	)
}

source_release() {
	local outdir="$1/"
	echo "-----"
	echo "Make Coref source release to $outdir"
	print_revisions

	copy_repository $outdir/Coref	
	( cd ./../LVTagger; echo ${PWD}; copy_repository $outdir/LVTagger )
	( cd ./../morphology; copy_repository $outdir/morphology)
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
