/**
 * Created by matthias on 08.01.16.
 */
module.exports = function(grunt) {
    grunt.initConfig({
        clean: [
            './../webapp/fonts',
            './../webapp/css',
            './../webapp/js'
        ],
        grunt: {
            buildsome: {
                gruntfile: 'vendors/bootstrap/Gruntfile.js',
                tasks: 'dist'
            }
        },
        sass: {
            options: {
                sourceMap: true
            },
            dist: {
                files: {
                    './build/roboto-fontface.css':
                        './vendors/roboto-fontface/css/roboto-fontface.scss',
                    './build/font-awesome.css':
                        './vendors/font-awesome/scss/font-awesome.scss',
                    './build/exmatrikulator.css':
                        './sass/exmatrikulator.sass'
                }
            }
        },
        coffee: {
            compile: {
                files: {
                    './build/exmatrikulator.js':
                        './coffee/exmatrikulator.coffee'
                }
            }
        },
        concat: {
            cssvendors: {
                src: [
                    './build/roboto-fontface.css',
                    './build/font-awesome.css',
                    './vendors/bootstrap/dist/css/bootstrap.css'
                ],
                dest: './build/vendors.css'
            },
            jsvendors: {
                src: [
                    './vendors/jquery/dist/jquery.min.js',
                    './vendors/bootstrap/dist/js/bootstrap.min.js'
                ],
                dest: './../webapp/js/vendors.min.js'
            }
        },
        cssmin: {
            css: {
                files: {
                    './../webapp/css/vendors.min.css': [
                        './build/vendors.css'
                    ],
                    './../webapp/css/exmatrikulator.min.css': [
                        './build/exmatrikulator.css'
                    ]
                }
            }
        },
        copy: {
            roboto: {
                expand: true,
                src: './vendors/roboto-fontface/fonts/*',
                dest: './../webapp/fonts/',
                flatten: true,
                filter: 'isFile'
            },
            fontawesome: {
                expand: true,
                src: './vendors/font-awesome/fonts/*',
                dest: './../webapp/fonts/',
                flatten: true,
                filter: 'isFile'
            }
        },
        uglify: {
            options: {
                mangle: false,
                screwIE8: true
            },
            js: {
                files: {
                    './../webapp/js/exmatrikulator.min.js': [
                        './build/exmatrikulator.js'
                    ]
                }
            }
        },
        exec: {
            build_folder: 'rm -rf build'
        },
        compress: {
            main: {
                options: {
                    mode: 'gzip'
                },
                files: [
                    {
                        expand: true,
                        src: ['./../webapp/js/*.js'],
                        dest: '.',
                        ext: '.gz.js'
                    },
                    {
                        expand: true,
                        src: ['./../webapp/css/*.css'],
                        dest: '.',
                        ext: '.gz.css'
                    }
                ]
            }
        }
    });

    grunt.loadNpmTasks('grunt-grunt');
    grunt.loadNpmTasks('grunt-contrib-clean');
    grunt.loadNpmTasks('grunt-contrib-concat');
    grunt.loadNpmTasks('grunt-contrib-copy');
    grunt.loadNpmTasks('grunt-contrib-cssmin');
    grunt.loadNpmTasks('grunt-contrib-coffee');
    grunt.loadNpmTasks('grunt-contrib-uglify');
    grunt.loadNpmTasks('grunt-contrib-compress');
    grunt.loadNpmTasks('grunt-sass');
    grunt.loadNpmTasks('grunt-exec');

    grunt.registerTask(
        'build',
        [
            'clean',
            'grunt',
            'sass',
            'coffee',
            'concat',
            'copy',
            'cssmin',
            'uglify',
            'exec',
            'compress'
        ]
    );
};